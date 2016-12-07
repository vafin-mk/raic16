package ai

import canUse
import extensions.*
import model.*
import pathfinding.Lane
import wrapper.GameUnit
import wrapper.GameUnitType
import wrapper.GameWorld

class AI {

  fun decision() {
    val start = System.nanoTime()
    fightDecision()
    turnDecision()
    moveDecision()
    if (game.isSkillsEnabled && self.level > self.getSkills().size) {
      learnDecision(self.getSkills().size)
    }

    val end = System.nanoTime()
    val ms = (end - start) / 1_000_000
    if (ms > 10) {
      log("tick take to long : $ms")
    }
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
      if (!self.hastened() && self.canUse(ActionType.HASTE, game)) {
        move.action = ActionType.HASTE
        return
      } else if (!self.shielded() && self.canUse(ActionType.SHIELD, game)) {
        move.action = ActionType.SHIELD
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
      if (self.canUse(ActionType.FROST_BOLT, game) && !target.unit.frozen()) {
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
    when(target.unit) {
      is Building -> coefficient += 1
      is Wizard -> coefficient += 0.5
      else -> coefficient += 0
    }
    return (100 - target.unit.hpPercent()) * coefficient
  }

  private fun turnDecision() {
    val targets = gameWorld.inRange(self.castRange, GameUnitType.ENEMY, GameUnitType.NEUTRAL)
    if (!targets.isEmpty()) {
      move.turn = evaluateBestTarget(targets).angle
      return
    }
    val moveTarget = lane.nextPoint(self.toPoint())
    move.turn = self.getAngleTo(moveTarget.x, moveTarget.y)
  }

  private fun moveDecision() {
    if (self.hpPercent() < 70 && gameWorld.inBattleZone(self)) {
      internalMove(MoveMode.BACKWARD)
      return
    }
    val enemies = gameWorld.inRange(self.castRange, GameUnitType.ENEMY, GameUnitType.NEUTRAL)
    if (enemies.isEmpty()) {
      internalMove(MoveMode.FORWARD)
      return
    }
    val allies = gameWorld.inRange(self.castRange, GameUnitType.ALLY).filterNot { it.unit is Wizard }
    if (allies.isEmpty()) {
      internalMove(MoveMode.BACKWARD)
      return
    }
    val closestEnemy = enemies.sortedBy { it.dist }[0]
    val closestAlly = allies.sortedBy { it.dist }[0]
    val shielded = closestAlly.unit.getDistanceTo(closestEnemy.unit) < closestEnemy.dist

    if (!shielded) {
      internalMove(MoveMode.BACKWARD)
      return
    }

    if (closestEnemy.dist > self.castRange * 0.8) {
      internalMove(MoveMode.FORWARD)
      return
    }
    //todo hunt almost death wizards
  }

  private fun internalMove(moveMode: MoveMode) {
    if (moveMode == MoveMode.STOP) {
      return
    }
    val target = if (moveMode == MoveMode.BACKWARD) lane.previousPoint(self.toPoint()) else lane.nextPoint(self.toPoint())
    val dist = target.dist(self.toPoint())
    val angle = self.getAngleTo(target.x, target.y)
    move.speed = dist * StrictMath.cos(angle)//todo min(dist/forward|bacward speed)
    move.strafeSpeed = dist * StrictMath.sin(angle)
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
    when(self.id.toInt()) {
      1, 2, 6, 7 -> lane = Lane.TOP
      4, 5, 9, 10 -> lane = Lane.BOTTOM
      3, 8 -> lane = Lane.MID
      else -> lane = Lane.MID
    }
  }

  private fun afterDeath() {
    lane.currentPoint = lane.path[0]
  }

  private fun log(text : String) {
    println("$text -- ${world.tickIndex}")
  }

  lateinit var self : Wizard
  lateinit var world : World
  lateinit var game : Game
  lateinit var move : Move

  var lastTick = 0
  lateinit var lane: Lane
  val gameWorld = GameWorld()
  val skillBuild = SkillBuild.SIMPLE
}