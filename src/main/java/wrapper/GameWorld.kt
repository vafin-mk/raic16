package wrapper

import extensions.opposite
import model.*
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
        && it.dist < game.staffRange
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

}