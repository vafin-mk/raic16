package pathfinding

import model.Wizard
import extensions.toPoint
import wrapper.GameUnit

class PotentialField(val attractive : Boolean, val target: Point, val initialCharge : Int,
                     val gradation :Int = 1, val coefficient: Int = 1, val initialDist : Double = 0.0) {

  fun force(self: Wizard, checkPoint: Point) : Double {
    val selfToCheckPoint = checkPoint.manhattanDist(self.toPoint())
    if (selfToCheckPoint < 1) {
      return Double.MIN_VALUE
    }
    val selfToTarget = self.toPoint().dist(target)
    val checkPointToTarget = checkPoint.manhattanDist(target)
    var force : Double
    if (initialDist < selfToTarget) {
      force = StrictMath.max(initialCharge - (initialDist + checkPointToTarget) * gradation, 0.0) / initialCharge
    } else {
      force = (checkPointToTarget - selfToTarget) / initialDist
    }

    force *= coefficient
    if (attractive) return force
    else return -force
  }
}