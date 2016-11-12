public class Vertex {
  final double x, y;
  Vertex(double x, double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Vertex vertex = (Vertex) o;
    return x == vertex.x && y == vertex.y;

  }

  @Override
  public int hashCode() {
    return (int) (4000 * x + y);
  }
}
