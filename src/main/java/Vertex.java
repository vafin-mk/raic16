import java.util.HashSet;
import java.util.Set;

public class Vertex implements Comparable<Vertex> {
  final double x, y;

  double g, h;
  Vertex parent;
  Set<Vertex> adjs = new HashSet<>();

  Vertex(double x, double y) {
    this.x = x;
    this.y = y;
  }

  private double getF() {
    return g + h;
  }

  @Override
  public int compareTo(Vertex o) {
    if (getF() < o.getF()) {
      return -1;
    } else if (getF() > o.getF()) {
      return 1;
    }
    return 0;
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

  @Override
  public String toString() {
    return String.format("%s/%s", x, y);
  }
}
