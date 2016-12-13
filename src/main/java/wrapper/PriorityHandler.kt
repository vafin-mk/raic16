package wrapper

import model.Move
import pathfinding.Point

class GamePoint(val point: Point, val priority: Double) : Comparable<GamePoint> {
  override fun compareTo(other: GamePoint): Int {
    return other.priority.compareTo(priority)
  }

  override fun toString(): String {
    return "$point-$priority"
  }
}

class GameMove(val move: Move, val priority: Double) : Comparable<GameMove> {
  override fun compareTo(other: GameMove): Int {
    return other.priority.compareTo(priority)
  }

  override fun toString(): String {
    return "$move-$priority"
  }
}

class GameTurn(val move: Move, val priority: Double) : Comparable<GameTurn> {
  override fun compareTo(other: GameTurn): Int {
    return other.priority.compareTo(priority)
  }

  override fun toString(): String {
    return "$move - $priority"
  }
}