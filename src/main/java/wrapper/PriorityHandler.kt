package wrapper

import model.Move
import pathfinding.Point

class GamePoint(val point: Point, val priority: Double) : Comparable<GamePoint> {
  override fun compareTo(other: GamePoint): Int {
    return priority.compareTo(other.priority)
  }
}

class GameMove(val move: Move, val priority: Double) : Comparable<GameMove> {
  override fun compareTo(other: GameMove): Int {
    return priority.compareTo(other.priority)
  }
}

class GameTurn(val move: Move, val priority: Double) : Comparable<GameTurn> {
  override fun compareTo(other: GameTurn): Int {
    return priority.compareTo(other.priority)
  }
}