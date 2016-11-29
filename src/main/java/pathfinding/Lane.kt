package pathfinding

import java.util.*

class Lane(vararg points : Point) {

  val path : ArrayList<Point>
  var currentPoint: Point

  init {
    path = ArrayList()
    path += points
    currentPoint = path[0]
  }

  fun nextPoint(from: Point) : Point {
    checkCurrentPoint(from)
    val index = path.indexOf(currentPoint)
    if (index < path.size - 1) return path[index + 1]
    return path[path.size - 1]
  }

  fun previousPoint(from: Point) : Point {
    checkCurrentPoint(from)
    val index = path.indexOf(currentPoint)
    if (index > 0) return path[index - 1]
    return path[0]
  }

  fun nearestPoint(from: Point) : Point {
    checkCurrentPoint(from)
    return path.sortedByDescending { from.dist(it) }[0]
  }

  private fun checkCurrentPoint(from: Point) {
    path.forEach { if (from.dist(it) < 20) currentPoint = it }
  }

  companion object{
    val TOP = Lane(
        Point.ALLY_FOUNTAIN, Point.ALLY_BASE_TOP_ENTRANCE, Point.ALLY_TOP_TOWER2.add(200.0, 100.0), Point.ALLY_TOP_TOWER1.add(-200.0, 100.0),
        Point.ALLY_TOP_CLASH_POINT, Point.ENEMY_TOP_CLASH_POINT, Point.ENEMY_TOP_TOWER1.add(-400.0, 200.0),
        Point.ENEMY_TOP_TOWER2.add(-400.0, -200.0), Point.ENEMY_BASE_TOP_ENTRANCE)
    val MID = Lane(
        Point.ALLY_FOUNTAIN, Point.ALLY_BASE_TOP_ENTRANCE, Point.ALLY_BASE_MID_ENTRANCE, Point.ALLY_MID_TOWER2.add(100.0, 200.0),
        Point.ALLY_MID_TOWER1.add(-200.0, 0.0), Point.MID_CLASH_POINT, Point.ENEMY_MID_TOWER1.add(200.0, 200.0),
        Point.ENEMY_MID_TOWER2.add(-400.0, -200.0), Point.ENEMY_BASE_MID_ENTRANCE)
    val BOTTOM = Lane(
        Point.ALLY_FOUNTAIN, Point.ALLY_BASE_BOT_ENTRANCE, Point.ALLY_BOT_TOWER2.add(-200.0, 200.0), Point.ALLY_BOT_TOWER1.add(-300.0, -150.0),
        Point.ALLY_BOT_CLASH_POINT, Point.ENEMY_BOT_CLASH_POINT, Point.ENEMY_BOT_TOWER1.add(200.0, 400.0),
        Point.ENEMY_BOT_TOWER2.add(-200.0, 500.0), Point.ENEMY_BASE_TOP_ENTRANCE)
    val RIVER = Lane(Point.ALLY_TOP_CLASH_POINT, Point.TOP_RUNE, Point.MID_CLASH_POINT, Point.BOT_RUNE, Point.ALLY_BOT_CLASH_POINT);
  }
  /*Vertex botAllyTower2 = Vertex.ALLY_BOT_TOWER2.add(-200, 200);
    Vertex botAllyTower1 = Vertex.ALLY_BOT_TOWER1.add(-300, -150);
    Vertex botAllyClashPoint = Vertex.ALLY_BOT_CLASH_POINT.copy();
    Vertex botEnemyClashPoint = Vertex.ENEMY_BOT_CLASH_POINT.copy();
    Vertex botEnemyTower1 = Vertex.ENEMY_BOT_TOWER1.add(200, 400);
Vertex botEnemyTower2 = Vertex.ENEMY_BOT_TOWER2.add(-200, 500);*/
}