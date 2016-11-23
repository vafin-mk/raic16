package ai;

import model.*;
import pathfinding.*;

import java.util.*;

import static java.lang.StrictMath.*;

//todo lane change, move turn and fight turn clash(missing shots there too maybe)
public class AI {

  private final Path TOP_LANE_PATH;
  private final Path MID_LANE_PATH;
  private final Path BOTTOM_LANE_PATH;

  private Graph navGraph;
  private PathFinder pathFinder;

  private Wizard self;
  private World world;
  private Game game;
  private Move move;

  private Random random;
  private Path currentPath;
  private PotentialField pf;

  private Vertex moveTarget;

  private List<LivingUnit> enemiesAround;

  public void updateWorldInfo(Wizard self, World world, Game game, Move move) {
    if (world.getTickIndex() == 0) {
        initData();
    }
    updateData(self, world, game, move);
    moveTarget = currentPath.nextVertex(self);
    enemiesAround = getAllEnemiesInRadius(game.getWizardCastRange() * 1.2, false);
    enemiesAround.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));
    pf.updateField(self, moveTarget, world, game);
  }

  private void initData() {
    random = new Random(game.getRandomSeed());
    switch ((int) self.getId()) {
      case 1:
      case 2:
      case 6:
      case 7:
          currentPath = TOP_LANE_PATH;
        break;
      case 3:
      case 8:
          currentPath = MID_LANE_PATH;
        break;
      case 4:
      case 5:
      case 9:
      case 10:
          currentPath = BOTTOM_LANE_PATH;
        break;
      default:
    }
    currentPath = testPath();
    log(Arrays.toString(currentPath.path.toArray()));
  }

  public void makeDecision() {
    long start = System.nanoTime();
//    checkBehaviour();
//    fightAction();
//    moveAction();
    fight2();
    move2();
    turn2();
    long tickHandleTime = (System.nanoTime() - start) / 1_000_000;
    if (tickHandleTime > 10) {
      log("Tick handled to long: %s ms!!", tickHandleTime);
    }
  }

  private void fight2() {

  }

  private void turn2() {
    move.setTurn(game.getWizardMaxTurnAngle());
  }

  private void move2() {
    Vertex target = pf.bestVertex();
    double angle = self.getAngleTo(target.x, target.y);
    move.setSpeed(game.getWizardForwardSpeed() * cos(angle));
    move.setStrafeSpeed(game.getWizardStrafeSpeed() * sin(angle));
  }

  private void moveAction() {
    //fight
    LivingUnit closestEnemy = enemiesAround.get(0);
    double closestEnemyDist = closestEnemy.getDistanceTo(self);
    if (closestEnemyDist < 200 || !haveAllyShield()) {
      moveTo(currentPath.nextVertex(self, false), false);
    } else if (closestEnemyDist > game.getWizardCastRange() * 0.9) {
      moveTo(currentPath.nextVertex(self));
    }
  }

  private void fightAction() {
    if (self.getRemainingActionCooldownTicks() > 0 || enemiesAround.isEmpty()) {
      return;
    }
    LivingUnit closestEnemy = enemiesAround.get(0);
    double closestEnemyDist = closestEnemy.getDistanceTo(self);

    if (closestEnemyDist < game.getStaffRange()
        && actionAvailable(ActionType.STAFF)) {
      double angle = self.getAngleTo(closestEnemy);
      if (abs(angle) < game.getStaffSector() / 2.0D) {
        move.setAction(ActionType.STAFF);
        return;
      }
    }

    LivingUnit enemy = bestTarget();
    if (enemy == null) {
      return;
    }
    double angle = self.getAngleTo(enemy);
    move.setTurn(angle);

    if (abs(angle) < game.getStaffRange() / 2.0D && actionAvailable(ActionType.MAGIC_MISSILE)) {
      move.setAction(ActionType.MAGIC_MISSILE);
      move.setCastAngle(angle);
      move.setMinCastDistance(self.getDistanceTo(enemy) - enemy.getRadius() + game.getMagicMissileRadius());
    }

    //todo other actions round 1
  }

  private void moveTo(LivingUnit unit) {
    moveTo(new Vertex(unit.getX(), unit.getY()));
  }

  private void moveTo(Vertex vertex) {
    moveTo(vertex, true);
  }

  private void moveTo(Vertex vertex, boolean forward) {
    double angle = self.getAngleTo(vertex.x, vertex.y);

    if (forward) {
      move.setTurn(angle);
      if (abs(angle) < PI / 4) {
        move.setSpeed(game.getWizardForwardSpeed());
      } else if (abs(angle) > 3 * PI / 4) {
        move.setSpeed(-game.getWizardBackwardSpeed());
      }

      move.setStrafeSpeed(3 * game.getWizardStrafeSpeed() * random.nextDouble() * (random.nextBoolean() ? 1 : -1));
      return;
    }
    double partPi = PI / 10;
    if (angle < -partPi && angle > -partPi * 9) {
      move.setStrafeSpeed(game.getWizardStrafeSpeed());
    } else if (angle > partPi && angle < partPi * 9) {
      move.setStrafeSpeed(-game.getWizardStrafeSpeed());
    }

    if (abs(angle) < PI / 2) {
      move.setSpeed(-game.getWizardBackwardSpeed());
    } else {
      move.setSpeed(game.getWizardForwardSpeed());
    }
  }

  private boolean actionAvailable(ActionType action) {
    return self.getRemainingActionCooldownTicks() == 0
        && self.getRemainingCooldownTicksByAction()[action.ordinal()] == 0;
  }

  private boolean haveAllyShield() {
    LivingUnit closestEnemy = enemiesAround.get(0);
    double distToEnemy = closestEnemy.getDistanceTo(self);
    int allyPower = 0, enemyPower = 0;

    for (LivingUnit unit : world.getMinions()) {
      if (unit.getFaction() == self.getFaction() && unit.getDistanceTo(closestEnemy) < distToEnemy) {
        allyPower += 100;
      } else if (unit.getFaction() != self.getFaction()) {
        double dist = unit.getDistanceTo(self);
        if (dist < self.getCastRange()) {
          enemyPower += 50;
        } else if (dist < self.getCastRange() / 3) {
          enemyPower += 150;
        }
      }
    }

    for (LivingUnit unit : world.getWizards()) {
      if (unit.getFaction() == self.getFaction() && unit.getDistanceTo(closestEnemy) < distToEnemy) {
        allyPower += 50;
      } else if (unit.getFaction() != self.getFaction()) {
        double dist = unit.getDistanceTo(self);
        if (dist < self.getCastRange()) {
          enemyPower += 100;
        } else if (dist < self.getCastRange() / 3) {
          enemyPower += 200;
        }
      }
    }
    return allyPower > enemyPower;
  }

  private LivingUnit bestTarget() {
    List<LivingUnit> enemies = getAllEnemiesInRadius(game.getWizardCastRange(), false);
    if (enemies.isEmpty()) {
      enemies = getAllEnemiesInRadius(game.getWizardCastRange(), true);
    }
    if (enemies.isEmpty()) {
      return null;
    }
    LivingUnit bestEnemy = enemies.get(0);
    int bestEnemyValue = 0;
    for (LivingUnit enemy : enemies) {
      int value = enemy.getMaxLife() / enemy.getLife();
      if (enemy instanceof Wizard) {
        value *= 3;
      }
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
          && unit.getFaction() == oppositeFaction()
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
  public AI() {
    long start = System.nanoTime();
    navGraph = GraphFactory.buildNavigationGraph();
    log("building nav graph in %s ms", (System.nanoTime() - start) / 1_000_000);
    pathFinder = new PathFinder();

    TOP_LANE_PATH = pathFinder.buildPath(new Vertex(50, 3950), new Vertex(150, 300), navGraph);
    TOP_LANE_PATH.path.addAll(pathFinder.buildPath(new Vertex(900, 100), new Vertex(3300, 200), navGraph).path);
    MID_LANE_PATH = pathFinder.buildPath(new Vertex(50, 3950), new Vertex(3300, 800), navGraph);
    BOTTOM_LANE_PATH = pathFinder.buildPath(new Vertex(50, 3950), new Vertex(3800, 3900), navGraph);
    BOTTOM_LANE_PATH.path.addAll(pathFinder.buildPath(new Vertex(3900, 3500), new Vertex(3800, 800), navGraph).path);
  }

  private void updateData(Wizard self, World world, Game game, Move move) {
    this.self = self;
    this.world = world;
    this.game = game;
    this.move = move;
  }


  private Path testPath() {
    Vertex start = new Vertex(200, 3600);
    Vertex end = new Vertex(400, 3800);
    List<Vertex> path = new ArrayList<>();
    path.add(start);
    path.add(new Vertex(400, 3400));
    path.add(new Vertex(600, 3600));
    path.add(end);
    return new Path(start, end, path);
  }

  private Faction oppositeFaction() {
    return self.getFaction() == Faction.ACADEMY ? Faction.RENEGADES : Faction.ACADEMY;
  }

  private void log(String format, Object...args) {
    System.out.println(String.format(format, args) + " --- " + (world == null ? "init" : world.getTickIndex()));
  }
}
