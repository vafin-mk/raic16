package ai

import canUse
import extensions.*
import model.*
import pathfinding.Lane
import pathfinding.Point
import pathfinding.PotentialField
import wrapper.*
import java.util.*
import kotlin.collections.ArrayList

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
    fightPriority.clear()
    if (self.canUse(ActionType.STAFF, game)) {
      fightPriority.add(addStaffMove())
    }
    if (gameWorld.inBattleZone(self)) {
      gameWorld.inAngle(self.castRange, game.staffSector / 2, GameUnitType.ALLY)
          .filter { it.unit is Wizard }
          .forEach { wizard ->
            if (self.canUse(ActionType.HASTE, game)) fightPriority.add(addHasteMove(wizard.unit as Wizard))
            if (self.canUse(ActionType.SHIELD, game)) fightPriority.add(addShieldMove(wizard.unit as Wizard))
      }
      if (self.canUse(ActionType.HASTE, game)) fightPriority.add(addHasteMove(self))
      if (self.canUse(ActionType.SHIELD, game)) fightPriority.add(addShieldMove(self))
    }

    var targets = gameWorld.inAngle(self.castRange, game.staffSector / 2, GameUnitType.ENEMY)
    if (targets.isEmpty()) {
      targets = gameWorld.inAngle(self.castRange / 3, game.staffSector / 2, GameUnitType.NEUTRAL)
    }
    targets.forEach { target ->
      if (self.canUse(ActionType.FROST_BOLT, game)) fightPriority.add(addFrostMove(target))
      if (self.canUse(ActionType.FIREBALL, game)) fightPriority.add(addFireBallMove(target))
      if (self.canUse(ActionType.MAGIC_MISSILE, game)) fightPriority.add(addMagigMissileMove(target))
    }
    if (fightPriority.isEmpty()) return
    val bestMove = fightPriority.poll()

    move.action = bestMove.move.action
    move.statusTargetId = bestMove.move.statusTargetId
    move.castAngle = bestMove.move.castAngle
    move.minCastDistance = bestMove.move.minCastDistance
  }

  private fun addStaffMove() : GameMove {
    //&& gameWorld.haveStaffTarget(self, game)
    val mv = Move()
    mv.action = ActionType.STAFF
    var priority = 0.0
    return GameMove(mv, priority)
  }

  private fun addHasteMove(target: Wizard) : GameMove {
    val mv = Move()
    mv.action = ActionType.HASTE
    mv.castAngle = target.angle
    if (!target.isMe) move.statusTargetId = target.id else -1
    var priority = 0.0
    return GameMove(mv, priority)
  }

  private fun addShieldMove(target: Wizard) : GameMove {
    val mv = Move()
    mv.action = ActionType.SHIELD
    mv.castAngle = target.angle
    if (!target.isMe) move.statusTargetId = target.id else -1
    var priority = 0.0
    return GameMove(mv, priority)
  }

  private fun addFireBallMove(target: GameUnit) : GameMove{
    val mv = Move()
    mv.action = ActionType.FIREBALL
    mv.castAngle = target.angle
    mv.minCastDistance = target.dist - target.unit.radius + game.frostBoltRadius
    var priority = 0.0
    return GameMove(mv, priority)
  }

  private fun addFrostMove(target: GameUnit) : GameMove {
    val mv = Move()
    mv.action = ActionType.FROST_BOLT
    mv.castAngle = target.angle
    mv.minCastDistance = target.dist - target.unit.radius + game.frostBoltRadius
    var priority = 0.0
    return GameMove(mv, priority)
  }

  private fun addMagigMissileMove(target: GameUnit) : GameMove {
    val mv = Move()
    mv.action = ActionType.MAGIC_MISSILE
    mv.castAngle = target.angle
    mv.minCastDistance = target.dist - target.unit.radius + game.frostBoltRadius
    var priority = 0.0
    return GameMove(mv, priority)
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
    var targets = gameWorld.inRange(self.castRange, GameUnitType.ENEMY, GameUnitType.ALLY, GameUnitType.NEUTRAL, GameUnitType.TREE)
    if (targets.isEmpty()) {
      return
    }
    turnPriority.clear()
    targets.forEach { target ->
      turnPriority.add(evaluateTurn(target))
    }
    val bestTurn = turnPriority.poll()
    if (bestTurn.priority > 0) {
      move.turn = bestTurn.move.turn
    }
  }

  private fun evaluateTurn(target: GameUnit) : GameTurn {
    val mv = Move()
    mv.turn = target.angle
    val priority = 0.0
    return GameTurn(mv, priority)
  }

  private fun moveDecision() {

    val moveCanditates = moveCandidates(self)


    if (moveCanditates.isEmpty()) return
    movePriority.clear()
    moveCanditates.forEach { point ->
      movePriority.add(evaluateMovePoint(point))
    }
    internalMove(movePriority.poll().point)

//    if (self.hpPercent() < 25) {
//      internalMove(MoveMode.BACKWARD)
//      return
//    }
//    val enemies = gameWorld.inRange(self.castRange, GameUnitType.ENEMY, GameUnitType.NEUTRAL)
//    if (enemies.isEmpty()) {
//      internalMove(MoveMode.FORWARD)
//      return
//    }
//    if (enemies.filter { it.unit is Wizard && it.unit.hpPercent() < 25 }.isNotEmpty()) {
//      internalMove(MoveMode.FORWARD)
//      return
//    }
//    if (self.hpPercent() < 70 && gameWorld.inBattleZone(self)) {
//      internalMove(MoveMode.BACKWARD)
//      return
//    }
//    val allies = gameWorld.inRange(self.castRange, GameUnitType.ALLY).filterNot { it.unit is Wizard }
//    if (allies.isEmpty()) {
//      internalMove(MoveMode.BACKWARD)
//      return
//    }
//    val closestEnemy = enemies.sortedBy { it.dist }[0]
//    val closestAlly = allies.filter { it.unit !is Wizard }.sortedBy { it.dist }[0]
//    val shielded = closestAlly.unit.getDistanceTo(closestEnemy.unit) * 1.2 < closestEnemy.dist
//
//    if (!shielded) {
//      internalMove(MoveMode.BACKWARD)
//      return
//    }
//
//    if (closestEnemy.dist > self.castRange * 0.6) {
//      internalMove(MoveMode.FORWARD)
//      return
//    }
  }

  val pt = PotentialField(true, Point.MID_CLASH_POINT, 12000)
  private fun evaluateMovePoint(point: Point) : GamePoint {
    var priority = 0.0
    potentialFields.forEach { pf -> priority += pf.force(self, point) }
    priority += pt.force(self, point) * 100
    return GamePoint(point, priority)
  }

  private fun internalMove(point: Point) {
    val dist = point.dist(self.toPoint())
    val angle = self.getAngleTo(point.x, point.y)
    move.speed = dist * StrictMath.cos(angle)//todo min(dist/forward|bacward speed)
    move.strafeSpeed = dist * StrictMath.sin(angle)
  }

  private fun moveCandidates(self: Wizard) : MutableList<Point> {
    val res = ArrayList<Point>()
    val curr = self.toPoint()
    val range = 6
    for (x in Math.max(50, curr.x.toInt() - range)..Math.min(3950, curr.x.toInt() + range)) {
      //todo speed
      (Math.max(50, curr.y.toInt() - range)..Math.min(3950, curr.y.toInt() + range))
          .map { Point(x.toDouble(), it.toDouble()) }
          .filterTo(res) { it.dist(self.toPoint()) <= 4 }
    }

    val obstacles = gameWorld.obstacles(self)
    val blocked = ArrayList<Point>()
    obstacles.forEach { obstacle ->
      res.forEach { point ->
        if (obstacle.unit.toPoint().dist(point) < obstacle.unit.radius + self.radius + 8) {
          blocked.add(point)
        }
      }
    }
    res.removeAll(blocked)

    return res
  }

  private fun learnDecision(level: Int) {
    move.skillToLearn = skillBuild.build[level]
  }

  fun updateInfo(self: Wizard, world: World, game: Game, move: Move) {
    updateVars(self, world, game, move)
    gameWorld.update(world, self)
    potentialFields = gameWorld.potentialFields(self)
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

    if (game.isRawMessagesEnabled) gameMode = GameMode.TEAM
    else if (game.isSkillsEnabled) gameMode = GameMode.SINGLE_SKILL
    else gameMode = GameMode.SINGLE_NO_SKILL

    lane = gameMode.getLane(self.id.toInt())
    skillBuild = gameMode.getSkillBuild(self.id.toInt())
  }

  private fun afterDeath() {
    if (world.tickIndex >= 14000) {
      lane = Lane.MID
    }
    lane.currentPoint = lane.path[0]
  }

  private fun log(text : String) {
    println("$text -- ${world.tickIndex}")
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
  lateinit var gameMode : GameMode
  val movePriority  = PriorityQueue<GamePoint>()
  val fightPriority  = PriorityQueue<GameMove>()
  val turnPriority  = PriorityQueue<GameTurn>()
  var potentialFields : MutableList<PotentialField> = ArrayList()
}