package wrapper

import ai.GameMode
import extensions.opposite
import extensions.toPoint
import model.*
import pathfinding.PFType
import pathfinding.Point
import pathfinding.PotentialField
import java.util.*

class GameWorld {

  val FULL_UPDATE_RATE = 1
  val SIMPLE_UPDATE_RATE = 5

  fun update(world: World, self : Wizard) {
    if (world.tickIndex % FULL_UPDATE_RATE == 0) {
      fullUpdate(world, self)
      return
    }
//    if (world.tickIndex % SIMPLE_UPDATE_RATE == 0) {
//      all.forEach { it.update(self) }
//    }
  }

  fun inRange(range: Double, vararg types: GameUnitType) : List<GameUnit> {
    return all.filter { it.dist <= range && types.contains(it.type) }
  }

  fun inAngle(range: Double, angle : Double, vararg types: GameUnitType) : List<GameUnit> {
    return all.filter { it.dist <= range && StrictMath.abs(it.angle) < angle && types.contains(it.type) }
  }

  fun haveStaffTarget(self: Wizard, game: Game) : Boolean {
    return all.filter {
      it.type != GameUnitType.ALLY
        && it.dist < game.staffRange + it.unit.radius
          && StrictMath.abs(it.angle) < game.staffSector / 2
    }.isNotEmpty()
  }

  fun inBattleZone(self: Wizard) : Boolean {
    return all.filter {
      it.type != GameUnitType.ALLY
      && ((it.type == GameUnitType.ENEMY && ((it.unit is Wizard && it.dist < 600) || it.dist < 400))
          || (it.type == GameUnitType.NEUTRAL && it.dist < 250))
    }.isNotEmpty()
  }

  private fun fullUpdate(world: World, self : Wizard) {
    cleanWorld()

    world.getBuildings().forEach { building ->
      val unit = GameUnit(building, self)
      all.add(unit)
      when(building.faction) {
        self.faction -> allies.add(unit)
        self.faction.opposite() -> enemies.add(unit)
        else -> print("fail!")
      }
    }

    world.getMinions().forEach { minion ->
      val unit = GameUnit(minion, self)
      all.add(unit)
      when(minion.faction) {
        self.faction -> allies.add(unit)
        self.faction.opposite() -> enemies.add(unit)
        else -> neutrals.add(unit)
      }
    }

    world.getTrees().forEach { tree ->
      val unit = GameUnit(tree, self)
      all.add(unit)
      trees.add(unit)
    }

    world.getWizards().filterNot { it.isMe }.forEach { wizard ->
      val unit = GameUnit(wizard, self)
      all.add(unit)
      when(wizard.faction) {
        self.faction -> {
          allies.add(unit)
          allyWizards.add(unit)
        }
        self.faction.opposite() -> {
          enemies.add(unit)
          enemyWizards.add(unit)
        }
        else -> print("fail!")
      }
    }

    world.getProjectiles()
        .filter { it.getDistanceTo(self) < 1200 && StrictMath.abs(it.getAngleTo(self)) < StrictMath.PI / 4 }
        .forEach { projectiles.add(it)  }
  }

  fun obstacles(self: Wizard) : List<GameUnit> {
    return all.filter { it.dist < 300 }
  }

  fun potentialFields(self: Wizard, mode: GameMode, forEnemys : Boolean) : MutableList<PotentialField> {
    val res = ArrayList<PotentialField>()
    if (forEnemys) {
      all.filter { it.dist < self.castRange * 4 && it.unit.faction == self.faction.opposite() }
          .forEach { enemy ->
            val pos = enemy.unit.toPoint()
            when (enemy.unit) {
              is Wizard -> res.add(getPotentialField(self, enemy, PFType.ENEMY_WIZARD))
              is Building -> {
                if (enemy.unit.type == BuildingType.GUARDIAN_TOWER) res.add(getPotentialField(self, enemy, PFType.ENEMY_TOWER))
                else res.add(getPotentialField(self, enemy, PFType.ENEMY_BASE))
              }
              is Minion -> res.add(getPotentialField(self, enemy, PFType.ENEMY_MINION))
              //else -> res.add(PotentialField(false, pos, self.castRange.toInt()))
            }
          }
    } else {
      all.filter { it.dist < self.castRange * 2 && it.unit.faction == self.faction}.forEach { gameUnit ->
        val pos = gameUnit.unit.toPoint()
        val radius = gameUnit.unit.radius + self.radius + 15
        when (gameUnit.unit) {
          is Wizard -> res.add(getPotentialField(self, gameUnit, PFType.ALLY_WIZARD))
          is Building -> {
            if (gameUnit.unit.type == BuildingType.GUARDIAN_TOWER) res.add(getPotentialField(self, gameUnit, PFType.ALLY_TOWER))
            else res.add(getPotentialField(self, gameUnit, PFType.ALLY_BASE))
          }
          is Minion -> res.add(getPotentialField(self, gameUnit, PFType.ALLY_MINION))
          //else -> res.add(PotentialField(false, pos, radius.toInt()))
        }
      }

      all.filter { it.dist < self.castRange * 3 && it.unit.faction == Faction.NEUTRAL}.forEach { gameUnit ->
        val pos = gameUnit.unit.toPoint()
        val radius = gameUnit.unit.radius + self.radius + 15
        when (gameUnit.unit) {
          //is Minion -> res.add(getPotentialField(self, gameUnit, PFType.))
          is Tree -> res.add(getPotentialField(self, gameUnit, PFType.TREE))
          //else -> res.add(PotentialField(false, pos, radius.toInt()))
        }
      }
    }
    return res
  }

  private fun getPotentialField(self: Wizard, gameUnit: GameUnit, type: PFType): PotentialField {
    val avoidObstacleRange = self.radius + 10

    var attractive = true
    var point = gameUnit.unit.toPoint()
    var initialCharge = 100
    var gradation = 1
    var coefficent = 10
    var initialDist = 0.0
    when (type) {
      PFType.ENEMY_MINION -> {
        initialCharge = 1000
        initialDist = 100.0//todo staff range
        coefficent = 1
      }
      PFType.ENEMY_WIZARD -> {
        initialCharge = 1000
        initialDist = 300.0
        coefficent = 1
      }
      PFType.ENEMY_TOWER -> {
        initialCharge = 1000
        initialDist = 450.0
        coefficent = 1
      }
      PFType.ENEMY_BASE -> {
        initialCharge = 1000
        initialDist = 450.0
        coefficent = 1
      }
      PFType.ALLY_MINION -> {
        attractive = false
        initialCharge = (gameUnit.unit.radius + avoidObstacleRange).toInt()
      }
      PFType.ALLY_WIZARD -> {
        attractive = false
        initialCharge = (gameUnit.unit.radius + avoidObstacleRange).toInt()
      }
      PFType.ALLY_TOWER -> {
        attractive = false
        initialCharge = (gameUnit.unit.radius + avoidObstacleRange).toInt() * 2
      }
      PFType.ALLY_BASE -> {
        attractive = false
        initialCharge = (gameUnit.unit.radius + avoidObstacleRange).toInt()
      }
      PFType.TREE -> {
        attractive = false
        initialCharge = (gameUnit.unit.radius + avoidObstacleRange).toInt() * 5
        coefficent = 1
      }
    }
    return PotentialField(attractive, point, initialCharge, gradation, coefficent, initialDist)
  }

  private fun cleanWorld() {
    all.clear()
    enemies.clear()
    allies.clear()
    trees.clear()
    neutrals.clear()
    enemyWizards.clear()
    allyWizards.clear()
  }

  val all = ArrayList<GameUnit>()
  val enemies = ArrayList<GameUnit>()
  val allies = ArrayList<GameUnit>()
  val trees = ArrayList<GameUnit>()
  val neutrals = ArrayList<GameUnit>()
  val enemyWizards = ArrayList<GameUnit>()
  val allyWizards = ArrayList<GameUnit>()
  val projectiles = ArrayList<Projectile>()

}