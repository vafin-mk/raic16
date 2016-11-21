package ai;

import model.Unit;
import pathfinding.Vertex;

import java.util.Collection;

public class Utils {

  static double dist(double fromX, double fromY, double toX, double toY) {
    return StrictMath.hypot(fromX - toX, fromY - toY);
  }

  public static double dist(Vertex from, Vertex to) {
    return dist(from.x, from.y, to.x, to.y);
  }

  public static double dist(Unit from, Vertex to) {
    return dist(from.getX(), from.getY(), to.x, to.y);
  }

  public static Vertex closestVertex(Unit unit, Collection<Vertex> vertices) {
    return closestVertex(new Vertex(unit.getX(), unit.getY()), vertices);
  }

  public static Vertex closestVertex(Vertex origin, Collection<Vertex> vertices) {
    Vertex closest = null;
    double dist = 1_000_000;
    for (Vertex vertex : vertices) {
      if (closest == null) {
        closest = vertex;
      }
      double vertDist = dist(origin, vertex);
      if (vertDist < dist) {
        dist = vertDist;
        closest = vertex;
      }
    }
    return closest;
  }
}
