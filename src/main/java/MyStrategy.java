import model.*;

import java.util.*;

import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {

  private static final Vertex TOP_RUNE = new Vertex(1200, 1200);
  private static final Vertex BOT_RUNE = new Vertex(2800, 2800);

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
  private Path runePath;
  private Behaviour behaviour = Behaviour.MOVE;
  private List<LivingUnit> enemiesAround;

  private boolean topRuneVisited;
  private boolean botRuneVisited;

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
        if (!topRuneVisited && self.getDistanceTo(TOP_RUNE.x, TOP_RUNE.y) < game.getBonusRadius()) {
          topRuneVisited = true;
          runePath.path.remove(TOP_RUNE);
          log("TOP RUNE ACQUIRED RUNE PATH:", Arrays.toString(runePath.path.toArray()));
        } else if (!botRuneVisited && self.getDistanceTo(BOT_RUNE.x, BOT_RUNE.y) < game.getBonusRadius()) {
          botRuneVisited = true;
          runePath.path.remove(BOT_RUNE);
          log("BOT RUNE ACQUIRED RUNE PATH:", Arrays.toString(runePath.path.toArray()));
        }
        moveTo(runePath.nextVertex(self));
        return;
    }
  }

  private void checkBehaviour() {
    if (world.getTickIndex() % game.getBonusAppearanceIntervalTicks() == 0) {
      topRuneVisited = false;
      botRuneVisited = false;
    }
    if ((!topRuneVisited || !botRuneVisited)) {
      if (behaviour == Behaviour.RUNE) {
        if (world.getTickIndex() % game.getBonusAppearanceIntervalTicks() > game.getBonusAppearanceIntervalTicks() * 0.5) {
          topRuneVisited = true;
          botRuneVisited = true;
        }
        return;
      }
      Path toTop = pathFinder.buildPath(self, TOP_RUNE, navGraph);
      Path toBot = pathFinder.buildPath(self, BOT_RUNE, navGraph);
      double distTop = toTop.distTo(self, TOP_RUNE);
      double distBot = toBot.distTo(self, BOT_RUNE);
      log("DISTANCE TO TOP ", distTop);
      log("DISTANCE TO BOT ", distBot);
      boolean topCloser = distTop > distBot;
      topRuneVisited = distTop > 3000;
      botRuneVisited = distBot > 3000;
      if (topRuneVisited && botRuneVisited) {
        behaviour = Behaviour.MOVE;
        return;
      }
      log("BEHAVIOUD CHANGE FROM %s TO RUNE", behaviour);
      behaviour = Behaviour.RUNE;
      if (topRuneVisited) {
        runePath = toBot;
      } else if (botRuneVisited) {
        runePath = toTop;
      } else { //going for both
        runePath = pathFinder.buildPath(self, topCloser ? TOP_RUNE : BOT_RUNE, navGraph);
        Path other = pathFinder.buildPath(topCloser ? TOP_RUNE : BOT_RUNE, topCloser ? BOT_RUNE : TOP_RUNE, navGraph);
        other.path.remove(0);
        runePath.path.addAll(other.path);
      }
      runePath.clearRepeats();
      log("RUNE PATH: %s", Arrays.toString(runePath.path.toArray()));
      return;
    }
    if(!enemiesAround.isEmpty()) {
      if (behaviour != Behaviour.FIGHT) {
        log("BEHAVIOUD CHANGE FROM %s TO FIGHT", behaviour);
      }
      behaviour = Behaviour.FIGHT;
      return;
    }
    if (behaviour != Behaviour.MOVE) {
      log("BEHAVIOUD CHANGE FROM %s TO MOVE", behaviour);
    }
    behaviour = Behaviour.MOVE;
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

  private boolean action() {
    LivingUnit closestEnemy = enemiesAround.get(0);
    double closestEnemyDist = closestEnemy.getDistanceTo(self);

    if (closestEnemyDist < 200 || !haveAllyShield()) {
      moveTo(currentPath.nextVertex(self, false), false);
    } else if (closestEnemyDist > game.getWizardCastRange() * 0.9) {
      moveTo(closestEnemy);
    }

    if (closestEnemyDist < game.getStaffRange()
        && actionAvailable(ActionType.STAFF)) {
      double angle = self.getAngleTo(closestEnemy);
      if (abs(angle) < game.getStaffRange() / 2.0D) {
        move.setAction(ActionType.STAFF);
        return true;
      }
    }
    LivingUnit enemy = bestTarget();
    if (enemy == null) {
      return false;
    }
    double angle = self.getAngleTo(enemy);
    move.setTurn(angle);

    if (abs(angle) < game.getStaffRange() / 2.0D && actionAvailable(ActionType.MAGIC_MISSILE)) {
      move.setAction(ActionType.MAGIC_MISSILE);
      move.setCastAngle(angle);
      move.setMinCastDistance(self.getDistanceTo(enemy) - enemy.getRadius() + game.getMagicMissileRadius());
    }
    return true;
  }

  private boolean actionAvailable(ActionType action) {
    return self.getRemainingActionCooldownTicks() == 0
        && self.getRemainingCooldownTicksByAction()[action.ordinal()] == 0;
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

    TOP_LANE_PATH = pathFinder.buildPath(new Vertex(50, 3950), new Vertex(150, 300), navGraph);
    TOP_LANE_PATH.path.addAll(pathFinder.buildPath(new Vertex(900, 100), new Vertex(3300, 200), navGraph).path);
    MID_LANE_PATH = pathFinder.buildPath(new Vertex(50, 3950), new Vertex(3300, 800), navGraph);
    BOTTOM_LANE_PATH = pathFinder.buildPath(new Vertex(50, 3950), new Vertex(3800, 3900), navGraph);
    BOTTOM_LANE_PATH.path.addAll(pathFinder.buildPath(new Vertex(3900, 3500), new Vertex(3800, 800), navGraph).path);
  }

  private void updateData(Wizard self, World world, Game game, Move move) {
    if (random == null) {
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
      log(Arrays.toString(currentPath.path.toArray()));
    }
    this.self = self;
    this.world = world;
    this.game = game;
    this.move = move;
    this.enemiesAround = getAllEnemiesInRadius(game.getWizardCastRange() * 2, false);
    enemiesAround.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));
  }

  private void log(String format, Object...args) {
//    System.out.println(String.format(format, args) + " --- " + world.getTickIndex());
  }
}
