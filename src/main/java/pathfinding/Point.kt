package pathfinding

class Point(val x : Double, val y : Double) {

  fun dist(other: Point) : Double {
    return StrictMath.hypot(other.x - x, other.y - y)
  }

  fun manhattanDist(other: Point) : Double {
    return StrictMath.abs(other.x - x) + StrictMath.abs(other.y - y)
  }

  fun add(x : Double, y: Double) : Point {
    return Point(this.x + x, this.y + y)
  }

  override fun toString(): String {
    return "Point($x;$y)"
  }

  companion object{
    val TOP_RUNE = Point(1200.0, 1200.0)
    val BOT_RUNE = Point(2800.0, 2800.0)
    val CENTER = Point(2000.0, 2000.0)

    val ALLY_FOUNTAIN = Point(100.0, 3900.0)
    val ALLY_BASE = Point(400.0, 3600.0)
    val ALLY_BASE_MID_ENTRANCE = Point(800.0, 3400.0)
    val ALLY_BASE_TOP_ENTRANCE = Point(100.0, 3400.0)
    val ALLY_BASE_BOT_ENTRANCE = Point(800.0, 3900.0)
    val ALLY_TOP_TOWER1 = Point(350.0, 1656.0)
    val ALLY_TOP_TOWER2 = Point(50.0, 2693.0)
    val ALLY_MID_TOWER1 = Point(1929.0, 2400.0)
    val ALLY_MID_TOWER2 = Point(902.0, 2768.0)
    val ALLY_BOT_TOWER1 = Point(2312.0, 3950.0)
    val ALLY_BOT_TOWER2 = Point(1370.0, 3650.0)
    val ALLY_TOP_CLASH_POINT = Point(100.0, 200.0)
    val ALLY_BOT_CLASH_POINT = Point(3800.0, 3900.0)

    val MID_CLASH_POINT = Point(2000.0, 2000.0)

    val ENEMY_BASE = Point(3600.0, 400.0)
    val ENEMY_BASE_MID_ENTRANCE = Point(3200.0, 800.0)
    val ENEMY_BASE_TOP_ENTRANCE = Point(3200.0, 200.0)
    val ENEMY_BASE_BOT_ENTRANCE = Point(3800.0, 800.0)
    val ENEMY_TOP_TOWER1 = Point(1688.0, 50.0)
    val ENEMY_TOP_TOWER2 = Point(2630.0, 350.0)
    val ENEMY_MID_TOWER1 = Point(2071.0, 1600.0)
    val ENEMY_MID_TOWER2 = Point(3098.0, 1232.0)
    val ENEMY_BOT_TOWER1 = Point(3650.0, 2344.0)
    val ENEMY_BOT_TOWER2 = Point(3950.0, 1307.0)
    val ENEMY_TOP_CLASH_POINT = Point(900.0, 150.0)
    val ENEMY_BOT_CLASH_POINT = Point(3900.0, 3500.0)
  }
}