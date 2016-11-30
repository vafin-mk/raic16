package wrapper

import extensions.opposite
import model.LivingUnit
import model.Tree
import model.Wizard

class GameUnit(val unit: LivingUnit, self: Wizard) {

  var dist : Double = 0.0
  var angle : Double = 0.0
  val type : GameUnitType

  init {
    type = when(unit.faction) {
      self.faction -> GameUnitType.ALLY
      self.faction.opposite() -> GameUnitType.ENEMY
      else -> if (unit is Tree) GameUnitType.TREE else GameUnitType.NEUTRAL
    }
    update(self)
  }

  fun update(self: Wizard) {
    angle = self.getAngleTo(unit)
    dist = self.getDistanceTo(unit)
  }
}