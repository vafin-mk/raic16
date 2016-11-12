import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {

  private Graph navGraph;
  private PathFinder pathFinder;

  private Wizard self;
  private World world;
  private Game game;
  private Move move;

  private Random random;
  private Path currentPath;

  @Override
  public void move(Wizard self, World world, Game game, Move move) {
    updateData(self, world, game, move);
    if (self.getLife() < self.getMaxLife() / 3) {
      moveTo(currentPath.nextVertex(self, false));
      return;
    }
    if (!action()) {
      moveTo(currentPath.nextVertex(self));
    }
  }

  private void moveTo(Vertex vertex) {
    double angle = self.getAngleTo(vertex.x, vertex.y);

    move.setTurn(angle);

    if (abs(angle) < PI / 4) {
      move.setSpeed(game.getWizardForwardSpeed());
    } else if (abs(angle) > 3 * PI / 4) {
      move.setSpeed(game.getWizardBackwardSpeed());
    }

    move.setStrafeSpeed(3 * game.getWizardStrafeSpeed() * random.nextDouble() * (random.nextBoolean() ? 1 : -1));
  }

  private boolean action() {
    LivingUnit enemy = enemy();
    if (enemy == null) {
      return false;
    }
    double angle = self.getAngleTo(enemy);
    move.setTurn(angle);

    if (abs(angle) < game.getStaffSector() / 2.0D) {
      move.setAction(ActionType.MAGIC_MISSILE);
      move.setCastAngle(angle);
      move.setMinCastDistance(self.getDistanceTo(enemy) - enemy.getRadius() + game.getMagicMissileRadius());
    }
    return true;
  }

  private LivingUnit enemy() {
    List<LivingUnit> enemies = new ArrayList<>();
    enemies.addAll(enemiesInRadius(world.getBuildings(), game.getWizardCastRange()));
    enemies.addAll(enemiesInRadius(world.getWizards(), game.getWizardCastRange()));
    enemies.addAll(enemiesInRadius(world.getMinions(), game.getWizardCastRange()));
    if (enemies.isEmpty()) {
      return null;
    }
    LivingUnit bestEnemy = enemies.get(0);
    int bestEnemyValue = 0;
    for (LivingUnit enemy : enemies) {
      int value = enemy.getMaxLife() / enemy.getLife();
      if (value > bestEnemyValue) {
        bestEnemyValue = value;
        bestEnemy = enemy;
      }
    }
    return bestEnemy;
  }

  private List<LivingUnit> enemiesInRadius(LivingUnit[] units, double range) {
    List<LivingUnit> enemies = new ArrayList<>();
    for (LivingUnit unit : units) {
      if (unit.getDistanceTo(self) < range
          && unit.getFaction() != self.getFaction()
          && unit.getFaction() != Faction.OTHER
          && unit.getFaction() != Faction.NEUTRAL) {
        enemies.add(unit);
      }
    }
    return enemies;
  }

  //INITIAL STAGE
  MyStrategy() {
    navGraph = GraphFactory.buildNavigationGraph();
    pathFinder = new PathFinder();
    currentPath = pathFinder.buildPath(new Vertex(50, 3950), new Vertex(150, 300), navGraph);
    currentPath.path.addAll(pathFinder.buildPath(new Vertex(900, 100), new Vertex(3300, 200), navGraph).path);
    System.out.println(Arrays.toString(currentPath.path.toArray()));
  }

  private void updateData(Wizard self, World world, Game game, Move move) {
    if (random == null) {
      random = new Random(game.getRandomSeed());
    }
    this.self = self;
    this.world = world;
    this.game = game;
    this.move = move;
  }
}
