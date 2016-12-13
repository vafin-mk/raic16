package wrapper

import model.Faction
import model.LivingUnit
import model.Status

class DummyUnit(x: Double, y: Double,radius: Double)
  : LivingUnit(100500, x, y, 0.0, 0.0, 0.0, Faction.NEUTRAL, radius, 2, 2, emptyArray())