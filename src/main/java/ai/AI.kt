package ai

import canUse
import extensions.*
import model.*
import pathfinding.Lane
import pathfinding.Point
import wrapper.GameUnit
import wrapper.GameUnitType
import wrapper.GameWorld
import java.util.*

class AI {

  fun decision() {
    val start = System.nanoTime()
    fightDecision()
    turnDecision()
    moveDecision()
    if (game.isSkillsEnabled && self.level > self.getSkills().size) {
      learnDecision(self.getSkills().size)
    }

//    val end = System.nanoTime()
//    val ms = (end - start) / 1_000_000
//    if (ms > 10) {
//      log("tick take to long : $ms")
//    }
  }

  private fun fightDecision() {
    if (!self.canFight()) {
      return
    }
    if (self.canUse(ActionType.STAFF, game) && gameWorld.haveStaffTarget(self, game)) {
      move.action = ActionType.STAFF
      return
    }
    if (gameWorld.inBattleZone(self)) {
      gameWorld.allies.filter { it.unit is Wizard && it.dist < self.castRange}.forEach { ally ->
        if (!ally.unit.hastened() && self.canUse(ActionType.HASTE, game)) {
          move.action = ActionType.HASTE
          move.statusTargetId = ally.unit.id
          log("cast haste on ${move.statusTargetId}")
          return
        } else if (!ally.unit.shielded() && self.canUse(ActionType.SHIELD, game)) {
          move.action = ActionType.SHIELD
          move.statusTargetId = ally.unit.id
          log("cast shield on ${move.statusTargetId}")
          return
        }
      }
      if (!self.hastened() && self.canUse(ActionType.HASTE, game)) {
        move.action = ActionType.HASTE
        log("cast haste on ${move.statusTargetId}")
        return
      } else if (!self.shielded() && self.canUse(ActionType.SHIELD, game)) {
        move.action = ActionType.SHIELD
        log("cast shield on ${move.statusTargetId}")
        return
      }
    }

    var targets = gameWorld.inAngle(self.castRange, game.staffSector / 2, GameUnitType.ENEMY)
    if (targets.isEmpty()) {
      targets = gameWorld.inAngle(self.castRange, game.staffSector / 2, GameUnitType.NEUTRAL)
    }
    if (targets.isEmpty()) {
      return
    }
    val target = evaluateBestTarget(targets)
    move.castAngle = target.angle
    if (target.unit is Wizard || target.unit is Building) {
      if (self.canUse(ActionType.FROST_BOLT, game) && !target.unit.frozen() && target.unit is Wizard) {
        move.minCastDistance = target.dist - target.unit.radius + game.frostBoltRadius
        move.action = ActionType.FROST_BOLT
        return
      }
      if (self.canUse(ActionType.FIREBALL, game)) {
        move.minCastDistance = target.dist - target.unit.radius + game.fireballRadius
        move.action = ActionType.FIREBALL
        return
      }
    }
    if (self.canUse(ActionType.MAGIC_MISSILE, game)) {
      move.minCastDistance = target.dist - target.unit.radius + game.magicMissileRadius
      move.action = ActionType.MAGIC_MISSILE
      return
    }
  }

  private fun evaluateBestTarget(targets: List<GameUnit>): GameUnit {
    var bestTarget = targets[0]
    var bestPts = 0.0
    targets.forEach {
      val pts = targetValue(it)
      if (pts > bestPts) {
        bestPts = pts
        bestTarget = it
      }
    }
    return bestTarget
  }

  private fun targetValue(target: GameUnit) : Double {
    var coefficient = 1.0
    if (target.unit.life < game.magicMissileDirectDamage) {
      coefficient += 50
    }
    when(target.unit) {
      is Building -> coefficient += 5
      is Wizard -> coefficient += 20
      else -> coefficient += 0
    }
    return (100 - target.unit.hpPercent()) * coefficient
  }

  private fun turnDecision() {
    var targets = gameWorld.inRange(self.castRange, GameUnitType.ENEMY)
    if (!targets.isEmpty()) {
      move.turn = evaluateBestTarget(targets).angle
      return
    }
    targets = gameWorld.inRange(self.castRange * 1.4, GameUnitType.ENEMY, GameUnitType.NEUTRAL)
    if (!targets.isEmpty()) {
      move.turn = evaluateBestTarget(targets).angle
      return
    }
    val moveTarget = lane.nextPoint(self.toPoint())
    move.turn = self.getAngleTo(moveTarget.x, moveTarget.y)
  }

  private fun moveDecision() {
    if (self.hpPercent() < 25) {
      internalMove(MoveMode.BACKWARD)
      return
    }
    val enemies = gameWorld.inRange(self.castRange, GameUnitType.ENEMY, GameUnitType.NEUTRAL)
    if (enemies.isEmpty()) {
      internalMove(MoveMode.FORWARD)
      return
    }
    if (enemies.filter { it.unit is Wizard && it.unit.hpPercent() < 25 }.isNotEmpty()) {
      internalMove(MoveMode.FORWARD)
      return
    }
    if (self.hpPercent() < 70 && gameWorld.inBattleZone(self)) {
      internalMove(MoveMode.BACKWARD)
      return
    }
    val allies = gameWorld.inRange(self.castRange, GameUnitType.ALLY).filterNot { it.unit is Wizard }
    if (allies.isEmpty()) {
      internalMove(MoveMode.BACKWARD)
      return
    }
    val closestEnemy = enemies.sortedBy { it.dist }[0]
    val closestAlly = allies.filter { it.unit !is Wizard }.sortedBy { it.dist }[0]
    val shielded = closestAlly.unit.getDistanceTo(closestEnemy.unit) * 1.2 < closestEnemy.dist

    if (!shielded) {
      internalMove(MoveMode.BACKWARD)
      return
    }

    if (closestEnemy.dist > self.castRange * 0.6) {
      internalMove(MoveMode.FORWARD)
      return
    }
  }

  private fun internalMove(moveMode: MoveMode) {
    if (moveMode == MoveMode.STOP) {
      return
    }
    val target = if (moveMode == MoveMode.BACKWARD) lane.previousPoint(self.toPoint()) else lane.nextPoint(self.toPoint())
    var bestTarget = avoidObstaclePoint(target)

    val dist = bestTarget.dist(self.toPoint())
    val angle = self.getAngleTo(bestTarget.x, bestTarget.y)
    move.speed = dist * StrictMath.cos(angle)//todo min(dist/forward|bacward speed)
    move.strafeSpeed = dist * StrictMath.sin(angle)
  }

  private fun avoidObstaclePoint(target : Point) : Point {
    val obstacles = gameWorld.obstacles(self)
    val moveCanditates = moveCandidates(self)
    val currDist = self.toPoint().dist(target)
    var best = self.toPoint()
    var bestScore = 0.0
    moveCanditates.forEach { point ->
      var score = 0.0
      score += currDist - point.dist(target)
      obstacles.forEach { obstacle ->
        if (obstacle.unit.toPoint().dist(point) < obstacle.unit.radius + self.radius * 1.2) {
          score = -10000000.0
        }
      }
      if (score > bestScore) {
        bestScore = score
        best = point
      }
    }
    return best
  }

  private fun moveCandidates(self: Wizard) : List<Point> {
    val res = ArrayList<Point>()
    val curr = self.toPoint()
    for (x in Math.max(50, curr.x.toInt() - 4)..Math.min(3950, curr.x.toInt() + 4)) {
      //todo speed
      (Math.max(50, curr.y.toInt() - 4)..Math.min(3950, curr.y.toInt() + 4))
          .map { Point(x.toDouble(), it.toDouble()) }
          .filterTo(res) { it.dist(self.toPoint()) <= 4 }
    }
    return res
  }

  private fun learnDecision(level: Int) {
    move.skillToLearn = skillBuild.build[level]
  }

  fun updateInfo(self: Wizard, world: World, game: Game, move: Move) {
    updateVars(self, world, game, move)
    gameWorld.update(world, self)
    if (world.tickIndex - lastTick > 1000) {
      afterDeath()
    }
    lastTick = world.tickIndex
  }

  private fun updateVars(self: Wizard, world: World, game: Game, move: Move) {
    this.self = self
    this.world = world
    this.game = game
    this.move = move
    if (world.tickIndex == 0) {
      initStrategy()
    }
  }

  private fun initStrategy() {
    random = Random(game.randomSeed)
    if (game.isRawMessagesEnabled) {//5x5
      when(self.id.toInt()) {
        1, 6 -> lane = Lane.TOP
        5, 10 -> lane = Lane.BOTTOM
        else -> lane = Lane.MID
      }
      when(self.id.toInt()) {
        1, 10 -> skillBuild = SkillBuild.DMGHAST
        2, 9 -> skillBuild = SkillBuild.FIREFROST
        3, 8 -> skillBuild = SkillBuild.SHIELDFIRE
        4, 7 -> skillBuild = SkillBuild.HASTESHIELD
        5, 6 -> skillBuild = SkillBuild.FROSTDMG
        else -> skillBuild = randomBuild()
      }
      return
    }
    //solo
    when(self.id.toInt()) {
      1, 2, 6, 7 -> lane = Lane.TOP
      4, 5, 9, 10 -> lane = Lane.BOTTOM
      3, 8 -> lane = Lane.MID
      else -> lane = Lane.MID
    }
    skillBuild = randomBuild()
  }

  private fun randomBuild() : SkillBuild {
    val rnd = random.nextInt(4)
    log("rnd -> $rnd")
    return when (rnd) {
      0 -> SkillBuild.DMGHAST
      1 -> SkillBuild.FIREFROST
      2 -> SkillBuild.FROSTFIRE
      3 -> SkillBuild.FROSTDMG
      4 -> SkillBuild.SHIELDFIRE
      5 -> SkillBuild.HASTESHIELD
      else -> SkillBuild.FROSTFIRE
    }
  }

  private fun afterDeath() {
    if (world.tickIndex >= 14000) {
      lane = Lane.MID
    }
    lane.currentPoint = lane.path[0]
  }

  private fun log(text : String) {
//    println("$text -- ${world.tickIndex}")
  }

  lateinit var self : Wizard
  lateinit var world : World
  lateinit var game : Game
  lateinit var move : Move
  lateinit var random : Random

  var lastTick = 0
  lateinit var lane: Lane
  val gameWorld = GameWorld()
  lateinit var skillBuild: SkillBuild
}