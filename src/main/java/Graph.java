import model.Wizard;

import java.util.*;

public class Graph {

  Set<Vertex> vertices;
  Set<Edge> edges;

  Graph(Collection<Vertex> vertices, Collection<Edge> edges) {
    this.vertices = new HashSet<>(vertices);
    this.edges = new HashSet<>(edges);
  }

  Graph(Collection<Edge> edges) {
    this(calc(edges), edges);
  }

  Vertex closest(Wizard self) {
    return closest(new Vertex(self.getX(), self.getY()));
  }

  Vertex closest(Vertex vertex) {
    Vertex closest = null;
    double dist = 100_500;
    for (Vertex vert : vertices) {
      if (closest == null) {
        closest = vert;
      }
      double dista = vert.dist(vertex);
      if (dista < dist) {
        dist = dista;
        closest = vert;
      }
    }
    return closest;
  }

  private static Collection<Vertex> calc(Collection<Edge> edges) {
    Set<Vertex> vertices = new HashSet<>();
    for (Edge edge : edges) {
      vertices.add(edge.first);
      vertices.add(edge.second);
    }
    return vertices;
  }
}
