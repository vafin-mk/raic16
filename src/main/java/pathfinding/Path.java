package pathfinding;

import model.Wizard;
import ai.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Path {
  final Vertex from, to;
  public final List<Vertex> path;
  Vertex lastVisited;
  public Path(Vertex from, Vertex to, List<Vertex> path) {
    this.from = from;
    this.to = to;
    this.path = path;
  }

  public Vertex nextVertex(Wizard self) {
    return nextVertex(self, true);
  }

  public Vertex nextVertex(Wizard self, boolean forward) {
    Vertex closest = Utils.closestVertex(self, path);
    int closIndex = path.indexOf(closest);
    boolean closestVisited = closest.equals(lastVisited);
    if (!closestVisited && Utils.dist(self, closest) < 10) {
      lastVisited = closest;
    }

    if (forward && closIndex < path.size() - 1 && closestVisited) {
      return path.get(closIndex + 1);
    } else if (!forward && closIndex > 0 && closestVisited) {
      return path.get(closIndex - 1);
    }
    return closest;
  }

  public double distTo(Wizard self, Vertex target) {
    Vertex closestStart = Utils.closestVertex(self, path);
    boolean begin = false;
    double dist = 0;

    for (int i = 0; i < path.size(); i++) {
      Vertex curr = path.get(i);
      if (!begin && (curr.equals(target) || curr.equals(closestStart))) {
        begin = true;
        continue;
      }
      if (begin) {
        dist += Utils.dist(curr, path.get(i - 1));
        if (curr.equals(target) || curr.equals(closestStart)) {
          return dist + self.getDistanceTo(closestStart.x, closestStart.y);
        }
      }
    }

    System.out.println(String.format("FAILED TO FIND DIST FROM %s TO %s within path %s",
        new Vertex(self.getX(), self.getY()), target, Arrays.toString(path.toArray())));
    return 100_500;
  }

  void clearRepeats() {
    Set<Vertex> uniqs = new HashSet<>(path);
    path.clear();
    path.addAll(uniqs);
  }
}
