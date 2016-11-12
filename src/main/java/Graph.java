import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Graph {

  Set<Vertex> vertices;
  Set<Edge> edges;

  Graph(Collection<Vertex> vertices, Collection<Edge> edges) {
    this.vertices = new HashSet<>(vertices);
    this.edges = new HashSet<>(edges);
  }
}
