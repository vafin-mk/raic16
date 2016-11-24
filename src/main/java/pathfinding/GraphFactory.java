package pathfinding;

import model.World;

import java.util.*;

public class GraphFactory {
  public static Graph buildNavigationGraph() {
    List<Vertex> vertices = new ArrayList<>();
    Vertex allyBaseFountain = Vertex.ALLY_FOUNTAIN.copy();
    Vertex allyBaseTopEntrance = Vertex.ALLY_BASE_TOP_ENTRANCE.copy();
    Vertex allyBaseMidEntrance = Vertex.ALLY_BASE_MID_ENTRANCE.copy();
    Vertex allyBaseBottomEntrance = Vertex.ALLY_BASE_BOT_ENTRANCE.copy();
    vertices.add(allyBaseFountain);
    vertices.add(allyBaseTopEntrance);
    vertices.add(allyBaseMidEntrance);
    vertices.add(allyBaseBottomEntrance);

    Vertex topAllyTower2 = Vertex.ALLY_TOP_TOWER2.add(200, 100);
    Vertex topAllyTower1 = Vertex.ALLY_TOP_TOWER1.add(-200, 100);
    Vertex topAllyClashPoint = Vertex.ALLY_TOP_CLASH_POINT.copy();
    Vertex topEnemyClashPoint = Vertex.ENEMY_TOP_CLASH_POINT.copy();
    Vertex topEnemyTower1 = Vertex.ENEMY_TOP_TOWER1.add(-400, 200);
    Vertex topEnemyTower2 = Vertex.ENEMY_TOP_TOWER2.add(-400, -200);
    vertices.add(topAllyTower2);
    vertices.add(topAllyTower1);
    vertices.add(topAllyClashPoint);
    vertices.add(topEnemyClashPoint);
    vertices.add(topEnemyTower1);
    vertices.add(topEnemyTower2);

    Vertex midAllyTower2 = Vertex.ALLY_MID_TOWER2.add(100, 200);
    Vertex midAllyTower1 = Vertex.ALLY_MID_TOWER1.add(-200, 0);
    Vertex midClashPoint = Vertex.MID_CLASH_POINT.copy();
    Vertex midEnemyTower1 = Vertex.ENEMY_MID_TOWER1.add(200, 200);
    Vertex midEnemyTower2 = Vertex.ENEMY_MID_TOWER2.add(-400, 50);
    vertices.add(midAllyTower2);
    vertices.add(midAllyTower1);
    vertices.add(midClashPoint);
    vertices.add(midEnemyTower1);
    vertices.add(midEnemyTower2);

    Vertex botAllyTower2 = Vertex.ALLY_BOT_TOWER2.add(-200, 200);
    Vertex botAllyTower1 = Vertex.ALLY_BOT_TOWER1.add(-300, -150);
    Vertex botAllyClashPoint = Vertex.ALLY_BOT_CLASH_POINT.copy();
    Vertex botEnemyClashPoint = Vertex.ENEMY_BOT_CLASH_POINT.copy();
    Vertex botEnemyTower1 = Vertex.ENEMY_BOT_TOWER1.add(200, 400);
    Vertex botEnemyTower2 = Vertex.ENEMY_BOT_TOWER2.add(-200, 500);
    vertices.add(botAllyTower2);
    vertices.add(botAllyTower1);
    vertices.add(botAllyClashPoint);
    vertices.add(botEnemyClashPoint);
    vertices.add(botEnemyTower1);
    vertices.add(botEnemyTower2);

    Vertex enemyBaseTopEntrance = Vertex.ENEMY_BASE_TOP_ENTRANCE.copy();
    Vertex enemyBaseMidEntrance = Vertex.ENEMY_BASE_MID_ENTRANCE.copy();
    Vertex enemyBaseBottomEntrance = Vertex.ENEMY_BASE_BOT_ENTRANCE.copy();
    vertices.add(enemyBaseTopEntrance);
    vertices.add(enemyBaseMidEntrance);
    vertices.add(enemyBaseBottomEntrance);

    Vertex topRune = Vertex.TOP_RUNE.copy();
    Vertex botRune = Vertex.BOT_RUNE.copy();
    vertices.add(topRune);
    vertices.add(botRune);

    List<Edge> edges = new ArrayList<>();
    //top lane
    edges.addAll(line(allyBaseFountain, allyBaseTopEntrance));
    edges.addAll(line(allyBaseTopEntrance, topAllyTower2));
    edges.addAll(line(topAllyTower2, topAllyTower1));
    edges.addAll(line(topAllyTower1, topAllyClashPoint));
    edges.addAll(line(topAllyClashPoint, topEnemyClashPoint));
    edges.addAll(line(topEnemyClashPoint, topEnemyTower1));
    edges.addAll(line(topEnemyTower1, topEnemyTower2));
    edges.addAll(line(topEnemyTower2, enemyBaseTopEntrance));
    //bottom lane
    edges.addAll(line(allyBaseFountain, allyBaseBottomEntrance));
    edges.addAll(line(allyBaseBottomEntrance, botAllyTower2));
    edges.addAll(line(botAllyTower2, botAllyTower1));
    edges.addAll(line(botAllyTower1, botAllyClashPoint));
    edges.addAll(line(botAllyClashPoint, botEnemyClashPoint));
    edges.addAll(line(botEnemyClashPoint, botEnemyTower1));
    edges.addAll(line(botEnemyTower1, botEnemyTower2));
    edges.addAll(line(botEnemyTower2, enemyBaseBottomEntrance));
    //mid lane
    edges.addAll(line(allyBaseTopEntrance, allyBaseMidEntrance));
    edges.addAll(line(allyBaseBottomEntrance, allyBaseMidEntrance));
    edges.addAll(line(allyBaseMidEntrance, midAllyTower2));
    edges.addAll(line(midAllyTower2, midAllyTower1));
    edges.addAll(line(midAllyTower1, midClashPoint));
    edges.addAll(line(midClashPoint, midEnemyTower1));
    edges.addAll(line(midEnemyTower1, midEnemyTower2));
    edges.addAll(line(midEnemyTower2, enemyBaseMidEntrance));
    edges.addAll(line(enemyBaseBottomEntrance, enemyBaseMidEntrance));
    edges.addAll(line(enemyBaseTopEntrance, enemyBaseMidEntrance));
    //river
    edges.addAll(line(topAllyClashPoint, topRune));
    edges.addAll(line(topRune, midClashPoint));
    edges.addAll(line(midClashPoint, botRune));
    edges.addAll(line(botRune, botAllyClashPoint));

    return new Graph(edges);
  }

  private static List<Edge> line(Vertex start, Vertex end) {
    List<Edge> edges = new ArrayList<>();
    double dist = start.dist(end);
    int step = 100;
    double len = step;
    Vertex curr = start;
    while (len < dist - step) {
      double ratio = len / dist;
      double x = ratio*end.x + (1.0 - ratio)*start.x;
      double y = ratio*end.y + (1.0 - ratio)*start.y;
      Vertex vert = new Vertex(x, y);
      edges.add(new Edge(curr, vert));
      curr = vert;
      len += step;
    }
    edges.add(new Edge(curr, end));
    return edges;
  }
}
