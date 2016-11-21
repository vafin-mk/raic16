package pathfinding;

import ai.Lane;
import model.Unit;

import java.util.HashSet;
import java.util.Set;

import static java.lang.StrictMath.hypot;

public class Vertex implements Comparable<Vertex> {
  //tower index from center
  public static final Vertex TOP_RUNE = new Vertex(1200, 1200);
  public static final Vertex BOT_RUNE = new Vertex(2800, 2800);

  public static final Vertex ALLY_BASE = new Vertex(400, 3600);
  public static final Vertex ALLY_BASE_MID_ENTRANCE = new Vertex(800, 3200);
  public static final Vertex ALLY_BASE_TOP_ENTRANCE = new Vertex(200, 3200);
  public static final Vertex ALLY_BASE_BOT_ENTRANCE = new Vertex(800, 3800);
  public static final Vertex ALLY_TOP_TOWER1 = new Vertex(350, 1656);
  public static final Vertex ALLY_TOP_TOWER2 = new Vertex(50, 2693);
  public static final Vertex ALLY_MID_TOWER1 = new Vertex(1929, 2400);
  public static final Vertex ALLY_MID_TOWER2 = new Vertex(902, 2768);
  public static final Vertex ALLY_BOT_TOWER1 = new Vertex(2312, 3950);
  public static final Vertex ALLY_BOT_TOWER2 = new Vertex(1370, 3650);

  public static final Vertex ENEMY_BASE = new Vertex(3600, 400);
  public static final Vertex ENEMY_BASE_MID_ENTRANCE = new Vertex(3200, 800);
  public static final Vertex ENEMY_BASE_TOP_ENTRANCE = new Vertex(3200, 200);
  public static final Vertex ENEMY_BASE_BOT_ENTRANCE = new Vertex(3800, 800);
  public static final Vertex ENEMY_TOP_TOWER1 = new Vertex(3650, 2344);
  public static final Vertex ENEMY_TOP_TOWER2 = new Vertex(3950, 1307);
  public static final Vertex ENEMY_MID_TOWER1 = new Vertex(2071, 1600);
  public static final Vertex ENEMY_MID_TOWER2 = new Vertex(3098, 1232);
  public static final Vertex ENEMY_BOT_TOWER1 = new Vertex(1688, 50);
  public static final Vertex ENEMY_BOT_TOWER2 = new Vertex(2630, 350);


  public final double x, y;
  public final Lane lane;

  double g, h;
  Vertex parent;
  Set<Vertex> adjs = new HashSet<>();

  public Vertex(double x, double y) {
    this.x = x;
    this.y = y;
    this.lane = findLane();
  }

  private Lane findLane() {
      if (x < 800 && y > 3200) {
        return Lane.BASE;
      } else if (x < 400 || y < 400) {
        return Lane.TOP;
      } else if (x > 3600 || y > 3600) {
        return Lane.BOT;
      } else if ((x >= 400 && x <= 2000 && y >= 2000 && y <= 3600)
          || (x >= 2000 && x <= 3600 && y >= 400 && y <= 2000)) {
        return Lane.MID;
      } else {
        return Lane.RIVER;
      }
  }

  double dist(double x, double y) {
    return hypot(x - this.x, y - this.y);
  }

  public double dist(Unit unit) {
    return dist(unit.getX(), unit.getY());
  }
  double dist(Vertex vertex) {
    return dist(vertex.x, vertex.y);
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

  Vertex copy() {
    return new Vertex(x, y);
  }
}
