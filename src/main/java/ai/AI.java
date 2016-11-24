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

  private Vertex moveTarget;

  private List<LivingUnit> enemies = new ArrayList<>();
  private List<LivingUnit> allies = new ArrayList<>();
  private List<Minion> neutrals = new ArrayList<>();
  private List<Tree> trees = new ArrayList<>();

  private LivingUnit closestTarget = null;
  private LivingUnit bestTarget = null;

  public void updateWorldInfo(Wizard self, World world, Game game, Move move) {
    updateData(self, world, game, move);
    if (world.getTickIndex() == 0) {
      initData();
    }
    updateUnitsAround(game.getWizardCastRange() * 3);
    boolean forward = true;
    if (closestTarget != null) {
      double dist = closestTarget.getDistanceTo(self);
      boolean shielded = haveAllyShield();
      if (closestTarget instanceof Wizard && !shielded) {
        forward = false;
      }

      double hpFactor = self.getLife() * 100.0 / self.getMaxLife();
      if (closestTarget.getFaction() == oppositeFaction() && dist < self.getCastRange() * 0.9
          && (!shielded || hpFactor < 60)) {
        forward = false;
      }
      if (hpFactor < 30 || dist < self.getCastRange() * 0.3) {
        forward = false;
      }
      //if (!forward && world.getTickIndex() % 50 == 0) {
      //  log("RETREAT ENEMY %s, DIST %s, shielded %s, hpFactor %s", closestTarget, dist, shielded, hpFactor);
      //}
    }
    moveTarget = currentPath.nextVertex(self, forward);
  }

  private void updateUnitsAround(double range) {
    enemies.clear();
    allies.clear();
    neutrals.clear();
    trees.clear();

    for (LivingUnit unit : world.getBuildings()) {
      if (unit.getDistanceTo(self) < range) {
        if (unit.getFaction() == self.getFaction()) {
          allies.add(unit);
        } else {
          enemies.add(unit);
        }
      }
    }

    for (Minion unit : world.getMinions()) {
      if (unit.getDistanceTo(self) < range) {
        if (unit.getFaction() == self.getFaction()) {
          allies.add(unit);
        } else if (unit.getFaction() == oppositeFaction()){
          enemies.add(unit);
        } else {
          neutrals.add(unit);
        }
      }
    }

    for (Tree unit : world.getTrees()) {
      if (unit.getDistanceTo(self) < range) {
        trees.add(unit);
      }
    }

    for (Wizard unit : world.getWizards()) {
      if (unit.getDistanceTo(self) < range && !unit.isMe()) {
        if (unit.getFaction() == self.getFaction()) {
          allies.add(unit);
        } else {
          enemies.add(unit);
        }
      }
    }

    enemies.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));
    allies.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));
    neutrals.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));
    trees.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));

    if (!enemies.isEmpty()) {
      closestTarget = enemies.get(0);
    } else if (!neutrals.isEmpty()) {
      closestTarget = neutrals.get(0);
    } else {
      closestTarget = null;
    }

    bestTarget = bestTarget();
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
//    currentPath = testPath();
    log(Arrays.toString(currentPath.path.toArray()));
  }

  public void makeDecision() {
    long start = System.nanoTime();
    fight();
    move();
    turn();
    learn();
    long tickHandleTime = (System.nanoTime() - start) / 1_000_000;
    if (tickHandleTime > 10) {
      log("Tick handled to long: %s ms!!", tickHandleTime);
    }
  }

  private void fight() {
    if (self.getRemainingActionCooldownTicks() > 0 || bestTarget == null) {
      return;
    }
    double closestEnemyDist = closestTarget.getDistanceTo(self);
    double closestAngle = self.getAngleTo(closestTarget);
    if (closestEnemyDist < game.getStaffRange()
        && actionAvailable(ActionType.STAFF)
        && abs(closestAngle) < game.getStaffSector() / 2.0D) {
        move.setAction(ActionType.STAFF);
        return;
    }

    double angle = self.getAngleTo(bestTarget);

    if (abs(angle) < game.getStaffRange() / 2.0D && actionAvailable(ActionType.MAGIC_MISSILE)) {
      move.setAction(ActionType.MAGIC_MISSILE);
      move.setCastAngle(angle);
      move.setMinCastDistance(self.getDistanceTo(bestTarget) - bestTarget.getRadius());
    }

    //todo other actions round 1
  }

  private void turn() {
    if (bestTarget != null) {
      move.setTurn(self.getAngleTo(bestTarget));
      return;
    }
    move.setTurn(self.getAngleTo(moveTarget.x, moveTarget.y));
  }

  private void move() {
    double dist = moveTarget.dist(self);
    double angle = self.getAngleTo(moveTarget.x, moveTarget.y);
    move.setSpeed(dist * cos(angle));
    move.setStrafeSpeed(dist * sin(angle));
  }

  private void learn() {

  }

  private boolean actionAvailable(ActionType action) {
    return self.getRemainingActionCooldownTicks() == 0
        && self.getRemainingCooldownTicksByAction()[action.ordinal()] == 0;
  }

  private boolean haveAllyShield() {
    if (closestTarget == null) {
      return true;
    }
    double distToEnemy = closestTarget.getDistanceTo(self);
    int allyPower = 0, enemyPower = 0;

    for (LivingUnit unit : world.getMinions()) {
      if (unit.getFaction() == self.getFaction() && unit.getDistanceTo(closestTarget) < distToEnemy) {
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
      if (unit.getFaction() == self.getFaction() && unit.getDistanceTo(closestTarget) < distToEnemy) {
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
    List<? extends LivingUnit> targets = enemies;
    if (targets.isEmpty()) {
      targets = neutrals;
    }
    if (targets.isEmpty()) {
      return null;
    }
    LivingUnit bestEnemy = targets.get(0);
    int bestEnemyValue = 0;
    for (LivingUnit enemy : targets) {
      if (enemy.getDistanceTo(self) > self.getCastRange()) {
        continue;
      }
      int value = enemy.getMaxLife() / enemy.getLife();
      if (enemy instanceof Wizard) {
        value *= 3;
      }
      if (value > bestEnemyValue) {
        bestEnemyValue = value;
        bestEnemy = enemy;
      }
    }
    if (bestEnemyValue == 0) {
      return null;
    }
    return bestEnemy;
  }

  //INITIAL STAGE
  public AI() {
    long start = System.nanoTime();
    navGraph = GraphFactory.buildNavigationGraph();
    log("building nav graph in %s ms", (System.nanoTime() - start) / 1_000_000);
    pathFinder = new PathFinder();
    List<Vertex> topPath = pathFinder.buildPath(Vertex.ALLY_FOUNTAIN, Vertex.ALLY_TOP_CLASH_POINT, navGraph);
    topPath.addAll(pathFinder.buildPath(Vertex.ENEMY_TOP_CLASH_POINT, Vertex.ENEMY_BASE_TOP_ENTRANCE.copy(), navGraph));
    TOP_LANE_PATH = new Path(Vertex.ALLY_FOUNTAIN, Vertex.ENEMY_BASE_TOP_ENTRANCE, topPath);

    MID_LANE_PATH = new Path(Vertex.ALLY_FOUNTAIN, Vertex.ENEMY_BASE_MID_ENTRANCE.copy(),
        pathFinder.buildPath(Vertex.ALLY_FOUNTAIN, Vertex.ENEMY_BASE_MID_ENTRANCE.copy(), navGraph));

    List<Vertex> botPath = pathFinder.buildPath(Vertex.ALLY_FOUNTAIN, Vertex.ALLY_BOT_CLASH_POINT, navGraph);
    botPath.addAll(pathFinder.buildPath(Vertex.ENEMY_BOT_CLASH_POINT, Vertex.ENEMY_BASE_BOT_ENTRANCE, navGraph));
    BOTTOM_LANE_PATH = new Path(Vertex.ALLY_FOUNTAIN.copy(), Vertex.ENEMY_BASE_BOT_ENTRANCE, botPath);
  }

  private void updateData(Wizard self, World world, Game game, Move move) {
    this.self = self;
    this.world = world;
    this.game = game;
    this.move = move;
  }


  private Path testPath() {

    Vertex start = new Vertex(Vertex.ALLY_BASE.x
        -game.getFactionBaseRadius()
        -game.getWizardRadius()
        -5,
        Vertex.ALLY_BASE.y);
    Vertex end = new Vertex(Vertex.ALLY_BASE.x,
        Vertex.ALLY_BASE.y
        +game.getFactionBaseRadius()
        +game.getWizardRadius()
    +5);
    List<Vertex> path = new ArrayList<>();
    path.add(start);

    path.add(new Vertex(Vertex.ALLY_BASE.x,
        Vertex.ALLY_BASE.y
            -game.getFactionBaseRadius()
            -game.getWizardRadius()
            -5));
    path.add(new Vertex(Vertex.ALLY_BASE.x
        +game.getFactionBaseRadius()
        +game.getWizardRadius() + 5,
        Vertex.ALLY_BASE.y));
    path.add(end);
    return new Path(start, end, path);
  }

  private Faction oppositeFaction() {
    return self.getFaction() == Faction.ACADEMY ? Faction.RENEGADES : Faction.ACADEMY;
  }

  private void log(String format, Object...args) {
   // System.out.println(String.format(format, args) + " --- " + (world == null ? "init" : world.getTickIndex()));
  }
}
