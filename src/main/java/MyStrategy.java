import model.*;

import java.util.*;

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
  private Behaviour behaviour = Behaviour.MOVE;
  private List<LivingUnit> enemiesAround;

  @Override
  public void move(Wizard self, World world, Game game, Move move) {
    updateData(self, world, game, move);
    checkBehaviour();
    switch (behaviour) {
      case MOVE:
        if (self.getLife() < self.getMaxLife() / 3) {
          moveTo(currentPath.nextVertex(self, false));
        } else {
          moveTo(currentPath.nextVertex(self));
        }
        return;
      case FIGHT:
        action();
        return;
      case RUNE:
        return;
    }
  }

  private void checkBehaviour() {
    if(!enemiesAround.isEmpty()) {
      behaviour = Behaviour.FIGHT;
      return;
    }
    behaviour = Behaviour.MOVE;
  }

  private void moveTo(LivingUnit unit) {
    moveTo(new Vertex(unit.getX(), unit.getY()));
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
    LivingUnit closestEnemy = enemiesAround.get(0);
    double closestEnemyDist = closestEnemy.getDistanceTo(self);

    if (closestEnemyDist < 200 || !haveAllyShield()) {
      move.setSpeed(-game.getWizardBackwardSpeed());
      move.setStrafeSpeed(random.nextBoolean() ? game.getWizardStrafeSpeed() : -game.getWizardStrafeSpeed());
    } else if (closestEnemyDist > game.getWizardCastRange() * 0.9) {
      moveTo(closestEnemy);
    }

    LivingUnit enemy = bestTarget();
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

  private boolean haveAllyShield() {
    LivingUnit closestEnemy = enemiesAround.get(0);
    double distToEnemy = closestEnemy.getDistanceTo(self);
    for (LivingUnit ally : world.getMinions()) {
      if (ally.getFaction() == self.getFaction() && ally.getDistanceTo(closestEnemy) < distToEnemy) {
        return true;
      }
    }
    return false;
  }

  private LivingUnit bestTarget() {
    List<LivingUnit> enemies = getAllEnemiesInRadius(game.getWizardCastRange(), false);
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

  private List<LivingUnit> enemiesInRadius(LivingUnit[] units, double range, boolean neutralsIncluded) {
    List<LivingUnit> enemies = new ArrayList<>();
    for (LivingUnit unit : units) {
      if (unit.getDistanceTo(self) < range
          && unit.getFaction() != self.getFaction()
          && unit.getFaction() != Faction.OTHER
          && (neutralsIncluded || unit.getFaction() != Faction.NEUTRAL)) {
        enemies.add(unit);
      }
    }
    return enemies;
  }

  private List<LivingUnit> getAllEnemiesInRadius(double range, boolean neutralsInluded) {
    List<LivingUnit> enemies = new ArrayList<>();
    enemies.addAll(enemiesInRadius(world.getBuildings(), range, neutralsInluded));
    enemies.addAll(enemiesInRadius(world.getWizards(), range, neutralsInluded));
    enemies.addAll(enemiesInRadius(world.getMinions(), range, neutralsInluded));
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
    this.enemiesAround = getAllEnemiesInRadius(game.getWizardCastRange() * 2, false);
    enemiesAround.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));
  }
}
