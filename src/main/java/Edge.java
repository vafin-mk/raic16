public class Edge {
  final Vertex first, second;
  Edge(Vertex first, Vertex second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Edge edge = (Edge) o;
    return (first.equals(edge.first) && second.equals(edge.second)) || (first.equals(edge.second) && second.equals(edge.first));

  }

  @Override
  public int hashCode() {
    int result = first.hashCode();
    result = 31 * result + second.hashCode();
    return result;
  }
}
