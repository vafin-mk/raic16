import java.util.ArrayList;
import java.util.List;

public class GraphFactory {
  static Graph buildNavigationGraph() {
    List<Vertex> vertices = new ArrayList<>();
    Vertex allyBaseFountain = new Vertex(100, 3900);
    Vertex allyBaseTopEntrance = new Vertex(100, 3300);
    Vertex allyBaseMidEntrance = new Vertex(600, 3300);
    Vertex allyBaseBottomEntrance = new Vertex(600, 3900);
    vertices.add(allyBaseFountain);
    vertices.add(allyBaseTopEntrance);
    vertices.add(allyBaseMidEntrance);
    vertices.add(allyBaseBottomEntrance);

    Vertex topAllyTower2 = new Vertex(300, 2800);
    Vertex topAllyTower1 = new Vertex(50, 1900);
    Vertex topAllyClashPoint = new Vertex(50, 200);
    Vertex topEnemyClashPoint = new Vertex(900, 100);
    Vertex topEnemyTower1 = new Vertex(1500, 300);
    Vertex topEnemyTower2 = new Vertex(2450, 100);
    vertices.add(topAllyTower2);
    vertices.add(topAllyTower1);
    vertices.add(topAllyClashPoint);
    vertices.add(topEnemyClashPoint);
    vertices.add(topEnemyTower1);
    vertices.add(topEnemyTower2);

    Vertex midAllyTower2 = new Vertex(850, 2900);
    Vertex midAllyTower1 = new Vertex(1700, 2400);
    Vertex midClashPoint = new Vertex(2000, 2000);
    Vertex midEnemyTower1 = new Vertex(2300, 1900);
    Vertex midEnemyTower2 = new Vertex(2650, 1300);
    vertices.add(midAllyTower2);
    vertices.add(midAllyTower1);
    vertices.add(midClashPoint);
    vertices.add(midEnemyTower1);
    vertices.add(midEnemyTower2);

    Vertex botAllyTower2 = new Vertex(1200, 3900);
    Vertex botAllyTower1 = new Vertex(2050, 3700);
    Vertex botAllyClashPoint = new Vertex(3800, 3900);
    Vertex botEnemyClashPoint = new Vertex(3900, 3500);
    Vertex botEnemyTower1 = new Vertex(3950, 2600);
    Vertex botEnemyTower2 = new Vertex(3750, 1600);
    vertices.add(botAllyTower2);
    vertices.add(botAllyTower1);
    vertices.add(botAllyClashPoint);
    vertices.add(botEnemyClashPoint);
    vertices.add(botEnemyTower1);
    vertices.add(botEnemyTower2);

    Vertex enemyBaseTopEntrance = new Vertex(3300, 200);
    Vertex enemyBaseMidEntrance = new Vertex(3300, 800);
    Vertex enemyBaseBottomEntrance = new Vertex(3800, 800);
    vertices.add(enemyBaseTopEntrance);
    vertices.add(enemyBaseMidEntrance);
    vertices.add(enemyBaseBottomEntrance);

    Vertex topRune = new Vertex(1200, 1200);
    Vertex botRune = new Vertex(2800, 2800);
    vertices.add(topRune);
    vertices.add(botRune);

    List<Edge> edges = new ArrayList<>();
    //top lane
    edges.add(new Edge(allyBaseFountain, allyBaseTopEntrance));
    edges.add(new Edge(allyBaseTopEntrance, topAllyTower2));
    edges.add(new Edge(topAllyTower2, topAllyTower1));
    edges.add(new Edge(topAllyTower1, topAllyClashPoint));
    edges.add(new Edge(topAllyClashPoint, topEnemyClashPoint));
    edges.add(new Edge(topEnemyClashPoint, topEnemyTower1));
    edges.add(new Edge(topEnemyTower1, topEnemyTower2));
    edges.add(new Edge(topEnemyTower2, enemyBaseTopEntrance));
    //bottom lane
    edges.add(new Edge(allyBaseFountain, allyBaseBottomEntrance));
    edges.add(new Edge(allyBaseBottomEntrance, botAllyTower2));
    edges.add(new Edge(botAllyTower2, botAllyTower1));
    edges.add(new Edge(botAllyTower1, botAllyClashPoint));
    edges.add(new Edge(botAllyClashPoint, botEnemyClashPoint));
    edges.add(new Edge(botEnemyClashPoint, botEnemyTower1));
    edges.add(new Edge(botEnemyTower1, botEnemyTower2));
    edges.add(new Edge(botEnemyTower2, enemyBaseBottomEntrance));
    //mid lane
    edges.add(new Edge(allyBaseTopEntrance, allyBaseMidEntrance));
    edges.add(new Edge(allyBaseBottomEntrance, allyBaseMidEntrance));
    edges.add(new Edge(allyBaseMidEntrance, midAllyTower2));
    edges.add(new Edge(midAllyTower2, midAllyTower1));
    edges.add(new Edge(midAllyTower1, midClashPoint));
    edges.add(new Edge(midClashPoint, midEnemyTower1));
    edges.add(new Edge(midEnemyTower1, midEnemyTower2));
    edges.add(new Edge(midEnemyTower2, enemyBaseMidEntrance));
    edges.add(new Edge(enemyBaseBottomEntrance, enemyBaseMidEntrance));
    edges.add(new Edge(enemyBaseTopEntrance, enemyBaseMidEntrance));
    //river
    edges.add(new Edge(topAllyClashPoint, topRune));
    edges.add(new Edge(topRune, midClashPoint));
    edges.add(new Edge(midClashPoint, botRune));
    edges.add(new Edge(botRune, botAllyClashPoint));

    return new Graph(vertices, edges);
  }
}
