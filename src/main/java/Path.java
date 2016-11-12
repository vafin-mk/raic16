import model.Wizard;

import java.util.List;

public class Path {
  final Vertex from, to;
  final List<Vertex> path;
  Vertex lastVisited;
  Path(Vertex from, Vertex to, List<Vertex> path) {
    this.from = from;
    this.to = to;
    this.path = path;
  }

  Vertex nextVertex(Wizard self) {
    return nextVertex(self, true);
  }

  Vertex nextVertex(Wizard self, boolean forward) {
    Vertex closest = Utils.closestVertex(self, path);
    int closIndex = path.indexOf(closest);
    boolean closestVisited = closest.equals(lastVisited);
    if (!closestVisited && Utils.dist(self, closest) < 10) {
      lastVisited = closest;
    }

    if (forward && closIndex < path.size() - 1 && closestVisited) {
      return path.get(closIndex + 1);
    } else if (!forward && closIndex < path.size() - 1 && closestVisited) {
      return path.get(closIndex + 1);
    }
    return closest;
  }
}
