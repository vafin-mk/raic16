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

  private static Collection<Vertex> calc(Collection<Edge> edges) {
    Set<Vertex> vertices = new HashSet<>();
    for (Edge edge : edges) {
      vertices.add(edge.first);
      vertices.add(edge.second);
    }
    return vertices;
  }
}
