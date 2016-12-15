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
    if (self.canUse(ActionType.STAFF, game) && gameWorld.haveStaffTarget(self, game)) {
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
      targets = gameWorld.inAngle(self.castRange / 1.3, game.staffSector / 2, GameUnitType.NEUTRAL)
    }
    targets.forEach { target ->
      if (self.canUse(ActionType.FROST_BOLT, game) && (self.mpPercent() > 60 || target.unit !is Minion)) fightPriority.add(addFrostMove(target))
      if (self.canUse(ActionType.FIREBALL, game) && (self.mpPercent() > 60 || target.unit !is Minion)) fightPriority.add(addFireBallMove(target))
      if (self.canUse(ActionType.MAGIC_MISSILE, game)) fightPriority.add(addMagicMissileMove(target))
    }
    if (fightPriority.isEmpty()) return
    val bestMove = fightPriority.poll()

    move.action = bestMove.move.action
    move.statusTargetId = bestMove.move.statusTargetId
    move.castAngle = bestMove.move.castAngle
    move.minCastDistance = bestMove.move.minCastDistance
  }

  private fun addStaffMove() : GameMove {
    val mv = Move()
    mv.action = ActionType.STAFF
    var priority = 20.0
    return GameMove(mv, priority)
  }

  private fun addHasteMove(target: Wizard) : GameMove {
    val mv = Move()
    mv.action = ActionType.HASTE
    mv.castAngle = target.angle
    if (!target.isMe) move.statusTargetId = target.id else -1
    var priority = 50.0 * target.hpPercent()
    return GameMove(mv, priority)
  }

  private fun addShieldMove(target: Wizard) : GameMove {
    val mv = Move()
    mv.action = ActionType.SHIELD
    mv.castAngle = target.angle
    if (!target.isMe) move.statusTargetId = target.id else -1
    var priority = 50.0 * target.hpPercent()
    return GameMove(mv, priority)
  }

  private fun addFireBallMove(target: GameUnit) : GameMove{
    val hpAttractive = 100 - target.unit.hpPercent() + 0.1
    val oneShoter = target.unit.life < game.magicMissileDirectDamage && StrictMath.abs(target.angle) < game.wizardMaxTurnAngle
    val mv = Move()
    mv.action = ActionType.FIREBALL
    mv.castAngle = target.angle
    mv.minCastDistance = target.dist - target.unit.radius / 2.5 // + game.fireballRadius
    mv.maxCastDistance = target.dist + target.unit.radius / 2.5 // - game.fireballRadius
    var priority = 0.0
    if (oneShoter) {
      priority += 100
    }
    //todo aoe!
    when (target.unit) {
      is Wizard -> {
        priority += 25 * hpAttractive
      }

      is Building -> {
        priority += 50 * hpAttractive
      }
    }
    return GameMove(mv, priority)
  }

  private fun addFrostMove(target: GameUnit) : GameMove {
    val hpAttractive = 100 - target.unit.hpPercent() + 0.1
    val mv = Move()
    mv.action = ActionType.FROST_BOLT
    mv.castAngle = target.angle
    mv.minCastDistance = target.dist - target.unit.radius / 2.5 // + game.frostBoltRadius
    var priority = 0.0

    when (target.unit) {
      is Wizard -> {
        priority += 40 * hpAttractive
      }
    }
    return GameMove(mv, priority)
  }

  private fun addMagicMissileMove(target: GameUnit) : GameMove {
    val hpAttractive = 100 - target.unit.hpPercent() + 0.1
    val oneShoter = target.unit.life < game.magicMissileDirectDamage && StrictMath.abs(target.angle) < game.wizardMaxTurnAngle
    val mv = Move()
    mv.action = ActionType.MAGIC_MISSILE
    mv.castAngle = target.angle
    mv.minCastDistance = target.dist - target.unit.radius / 2.5 // + game.magicMissileRadius
    var priority = 10.0
    if (oneShoter) {
      priority += 100
    }
    when (target.unit) {
      is Wizard -> {
        priority += 10 * hpAttractive
      }
      is Building -> {
        priority += 5 * hpAttractive
      }
    }
    return GameMove(mv, priority)
  }

  private fun turnDecision() {
    var targets = gameWorld.inRange(self.castRange, GameUnitType.ENEMY, GameUnitType.ALLY).filter { it.type == GameUnitType.ENEMY || it.unit is Wizard }
    if (targets.isEmpty()) {
      targets = gameWorld.inRange(self.castRange / 1.3, GameUnitType.NEUTRAL)
    }
    if (targets.isEmpty()) {
      targets = gameWorld.inRange(self.castRange, GameUnitType.TREE).filter { it.dist < 100 }
    }
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
    var priority = 0.0
    val hpAttractive = 100 - target.unit.hpPercent() + 0.1
    val oneShoter = target.unit.life < game.magicMissileDirectDamage && StrictMath.abs(target.angle) < game.wizardMaxTurnAngle
    if (oneShoter) {
      priority += 1000
    }
    if (target.type == GameUnitType.TREE && target.dist < 200) {
      priority += 5
    } else if (target.type == GameUnitType.ALLY) {
      if (self.canUse(ActionType.HASTE, game) && !target.unit.hastened()) {
        priority += 50
      } else {
        priority -= 1
      }
      if (self.canUse(ActionType.SHIELD, game) && !target.unit.shielded()) {
        priority += 50
      } else {
        priority -= 1
      }
    } else {
      when (target.unit) {
        is Minion -> {
          priority += hpAttractive * 5
        }
        is Wizard -> {
          priority += hpAttractive * 50
        }
        is Building -> {
          priority += hpAttractive * 25
        }
      }
    }
    return GameTurn(mv, priority)
  }

  private fun moveDecision() {
    val selfPoint = self.toPoint()

    if (self.hpPercent() < gameMode.getCoefficent(CoefType.RETREAT_HP_PERCENT_THRESHOLD)) {
      evaluateMoves(lane.previousPoint(selfPoint))
      return
    }
    var enemies = gameWorld.inRange(self.castRange * 1.2, GameUnitType.ENEMY)
    if (enemies.isEmpty()) {
      enemies = gameWorld.inRange(self.castRange / 3, GameUnitType.NEUTRAL)
    }
    if (enemies.isEmpty()) {
      evaluateMoves(lane.nextPoint(selfPoint))
      return
    }
    enemies.filter {
      (it.unit is Wizard && it.unit.hpPercent() < gameMode.getCoefficent(CoefType.ENEMY_WIZARD_HUNT_HP_PERCENT_THRESHOLD))
        || (it.unit is Building && it.unit.hpPercent() < gameMode.getCoefficent(CoefType.ENEMY_BUILDING_HUNT_HP_PERCENT_THRESHOLD)) }
        .sortedBy { it.unit.hpPercent() }
        .forEach { enemy ->
          when(enemy.unit) {
            is Wizard -> {
              if (enemy.dist > self.castRange / 2.5) {
                evaluateMoves(enemy.unit.toPoint())
              }
            }
            is Building -> evaluateMoves(enemy.unit.toPoint())
          }
          return
      }

//    if (self.hpPercent() < 70 && gameWorld.inBattleZone(self)) {
//      internalMove(MoveMode.BACKWARD)
//      return
//    }
    val allies = gameWorld.inRange(self.castRange * 1.5, GameUnitType.ALLY).filterNot { it.unit is Wizard || it.unit is Building}
    if (allies.isEmpty()) {
      evaluateMoves(lane.previousPoint(selfPoint))
      return
    }
    val closestEnemy = enemies.sortedBy { it.dist }[0]
    val closestAlly = allies.filter { it.unit !is Wizard }.sortedBy { it.dist }[0]
    val shielded = closestAlly.unit.getDistanceTo(closestEnemy.unit) * 1.2 < closestEnemy.dist

    if (!shielded) {
      evaluateMoves(lane.previousPoint(selfPoint))
      return
    }

    if (closestEnemy.dist > self.castRange * 0.7) {
      evaluateMoves(lane.nextPoint(selfPoint))
      return
    }
  }

  private fun evaluateMoves(targetPoint : Point) {
    val moveCanditates = moveCandidates(self)
    if (moveCanditates.isEmpty()) return
    movePriority.clear()
    moveCanditates.forEach { point ->
      movePriority.add(evaluateMovePoint(point, targetPoint))
    }
    internalMove(movePriority.poll().point)
  }

//  val pt = PotentialField(true, Point.MID_CLASH_POINT, 12000, coefficient = 10)
  private fun evaluateMovePoint(point: Point, targetPoint : Point) : GamePoint {
    var priority = 0.0
//    potentialFields.forEach { pf -> priority += pf.force(self, point) }
//    enemyPotentialFields.forEach { pf -> priority = StrictMath.max(pf.force(self, point), priority) }
//    priority += pt.force(self, point)
//    val mhDistPoint = point.manhattanDist(targetPoint)
//    val mhDistWizard = self.toPoint().manhattanDist(targetPoint)
  val mhDistPoint = point.dist(targetPoint)
  val mhDistWizard = self.toPoint().dist(targetPoint)
    return GamePoint(point, mhDistWizard - mhDistPoint)
  }

  private fun internalMove(point: Point) {
    val dist = point.dist(self.toPoint())
    val angle = self.getAngleTo(point.x, point.y)
    var spd = 4.0//todo forward speed
    if (StrictMath.abs(angle) > StrictMath.PI / 2) {
      spd = 3.0 //todo backward speed
    }
    move.speed = StrictMath.min(dist, spd) * StrictMath.cos(angle)//todo min(dist/forward|bacward speed)
    move.strafeSpeed = StrictMath.min(dist, spd) * StrictMath.sin(angle)
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
        if (obstacle.unit.toPoint().dist(point) < obstacle.unit.radius + self.radius * 1.2) {
          blocked.add(point)
        }
      }
    }
    blocked.addAll(enemyTopZone)
    blocked.addAll(enemyMidZone)
    blocked.addAll(enemyBotZone)
    res.removeAll(blocked)

    return res
  }

  private fun learnDecision(level: Int) {
    move.skillToLearn = skillBuild.build[level]
  }

  fun updateInfo(self: Wizard, world: World, game: Game, move: Move) {
    updateVars(self, world, game, move)
    gameWorld.update(world, self)
//    potentialFields = gameWorld.potentialFields(self, gameMode, false)
//    enemyPotentialFields = gameWorld.potentialFields(self, gameMode, true)
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

    lane = gameMode.getLane(self.id.toInt(), random)
    skillBuild = gameMode.getSkillBuild(self.id.toInt())
  }

  private fun afterDeath() {
    if (world.tickIndex >= 12000 || self.level > 10) {
      if (!laneChanged) {
        lane = Lane.MID.randomize(random, 25.0)
        laneChanged = true
      }
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
  val enemyTopZone : List<Point>
  val enemyMidZone : List<Point>
  val enemyBotZone : List<Point>
  var laneChanged = false
//  var potentialFields : MutableList<PotentialField> = ArrayList()
//  var enemyPotentialFields : MutableList<PotentialField> = ArrayList()

  init {
    enemyTopZone = ArrayList()
    for (x in 3050 until 3150) {
      (200 until 350).mapTo(enemyTopZone) { Point(x.toDouble(), it.toDouble()) }
    }
    enemyMidZone = ArrayList()
    for (x in 3150 until 3250) {
      (700 until 900).mapTo(enemyMidZone) { Point(x.toDouble(), it.toDouble()) }
    }
    enemyBotZone = ArrayList()
    for (x in 3700 until 3800) {
      (850 until 950).mapTo(enemyBotZone) { Point(x.toDouble(), it.toDouble()) }
    }
  }
}