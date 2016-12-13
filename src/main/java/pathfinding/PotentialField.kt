package pathfinding

import model.Wizard
import extensions.toPoint
import wrapper.GameUnit

class PotentialField(val attractive : Boolean, val target: Point, val initialCharge : Int) {

  fun force(self: Wizard, checkPoint: Point) : Double {
    val targetDist = checkPoint.manhattanDist(target)
    if (self.toPoint().dist(checkPoint) < 1) {
      return Double.MIN_VALUE
    }


    val force = StrictMath.max(initialCharge - targetDist, 0.0) / initialCharge
    if (attractive) return force
    else return -force
  }
}