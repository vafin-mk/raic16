import model.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StrictMath.*;
//todo lane change, move turn and fight turn clash(missing shots there too maybe)
public final class MyStrategy implements Strategy {

  private final Path TOP_LANE_PATH;
  private final Path MID_LANE_PATH;
  private final Path BOTTOM_LANE_PATH;

  private Graph navGraph;
  private PathFinder pathFinder;

  private Wizard self;
  private World world;
  private Game game;
  private Move move;

  private int ticksToRune;

  private Random random;
  private Path currentPath;
  private Path runePath;
  private Behaviour behaviour = Behaviour.MOVE;
  private List<LivingUnit> enemiesAround;
  private List<Minion> neutrals;
  private Lane currentLane = Lane.MID;

  private boolean runeVisited;

  private boolean enemyMidTower1Destroyed = false;
  private boolean enemyMidTower2Destroyed = false;
  private boolean enemyTopTower1Destroyed = false;
  private boolean enemyTopTower2Destroyed = false;
  private boolean enemyBotTower1Destroyed = false;
  private boolean enemyBotTower2Destroyed = false;

  @Override
  public void move(Wizard self, World world, Game game, Move move) {
    long start = System.nanoTime();
    updateData(self, world, game, move);
    checkBehaviour();
    fightAction();
    moveAction();
    long tickHandleTime = (System.nanoTime() - start) / 1_000_000;
    if (tickHandleTime > 10) {
      log("Tick handled to long: %s ms!!", tickHandleTime);
    }
  }

  private void moveAction() {
    if (behaviour == Behaviour.RUNE) {
      if ((Vertex.TOP_RUNE.dist(self) < game.getBonusRadius() || Vertex.BOT_RUNE.dist(self) < game.getBonusRadius()) && ticksToRune > 200) {
        runeVisited = true;
      }
      Vertex vertex = runePath.nextVertex(self);
      if (world.getTickIndex() % 100 == 0) log("RUNE -> %s", vertex);
      moveTo(vertex/*runePath.nextVertex(self)*/);
      return;
    }

    if (behaviour == Behaviour.MOVE) {
      if (self.getLife() > self.getMaxLife() / 2) {
        moveTo(currentPath.nextVertex(self));
      } else if (enemiesAround.isEmpty() || enemiesAround.get(0).getDistanceTo(self) > self.getCastRange() * 3 / 4) {
        //nothing, stay
      } else {
        moveTo(currentPath.nextVertex(self, false));
      }
      return;
    }

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

  private void checkBehaviour() {
    if (self.getLife() > self.getMaxLife() / 2 && behaviour == Behaviour.RUNE && !runeVisited) {
      return;
    }
    if (ticksToRune == 500) {
      Path toTop = pathFinder.buildPath(self, Vertex.TOP_RUNE, navGraph);
      Path toBot = pathFinder.buildPath(self, Vertex.BOT_RUNE, navGraph);
      double distTop = toTop.distTo(self, Vertex.TOP_RUNE);
      double distBot = toBot.distTo(self, Vertex.BOT_RUNE);
      if (distTop > 2000 && distBot > 2000) {
        return;
      }
      log("DISTANCE TO TOP ", distTop);
      log("DISTANCE TO BOT ", distBot);
      boolean topCloser = distTop > distBot;
      runePath = topCloser ? toTop : toBot;
      runeVisited = false;
      behaviour = Behaviour.RUNE;
      log("RUNE PATH: %s", Arrays.toString(runePath.path.toArray()));
      return;
    }
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
  MyStrategy() {
    long start = System.nanoTime();
    navGraph = GraphFactory.buildNavigationGraphV2();
    log("building nav graph in %s ms", (System.nanoTime() - start) / 1_000_000);
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
          currentLane = Lane.TOP;
          break;
        case 3:
        case 8:
          currentPath = MID_LANE_PATH;
          currentLane = Lane.MID;
          break;
        case 4:
        case 5:
        case 9:
        case 10:
          currentPath = BOTTOM_LANE_PATH;
          currentLane = Lane.BOT;
          break;
        default:
      }
      log(Arrays.toString(currentPath.path.toArray()));
    }
    this.self = self;
    this.world = world;
    this.game = game;
    this.move = move;

    if (world.getTickIndex() > 0) {
      this.ticksToRune = game.getBonusAppearanceIntervalTicks() - game.getBonusAppearanceIntervalTicks() % world.getTickIndex();
    }

    this.enemiesAround = getAllEnemiesInRadius(game.getWizardCastRange() * 1.2, false);
    enemiesAround.sort(Comparator.comparingDouble(unit -> unit.getDistanceTo(self)));

    if (world.getTickIndex() % 500 == 0) {
      navGraph = GraphFactory.buildNavigationGraphV2(world);
    }

    checkTowers();
    if (navGraph.closest(self).lane == Lane.BASE && world.getTickIndex() > 300) {
      changeLine();
    }
  }

  private void checkTowers() {
    boolean destroyed = false;
    for (Minion minion : world.getMinions()) {
      if (minion.getFaction() != self.getFaction()) {
        continue;
      }
      //top
      if (minion.getY() < 400) {
        if (minion.getX() >= Vertex.ENEMY_TOP_TOWER1.x + 20 && !enemyTopTower1Destroyed) {
          enemyTopTower1Destroyed = true;
          destroyed = true;
        }
        if (minion.getX() >= Vertex.ENEMY_TOP_TOWER2.x + 20 && !enemyTopTower2Destroyed) {
          enemyTopTower2Destroyed = true;
          destroyed = true;
        }
      }
      //bot
      if (minion.getX() > 3600) {
        if (minion.getY() < Vertex.ENEMY_BOT_TOWER1.x - 20 && !enemyBotTower1Destroyed) {
          enemyBotTower1Destroyed = true;
          destroyed = true;
        }
        if (minion.getY() < Vertex.ENEMY_BOT_TOWER2.x - 20 && !enemyBotTower2Destroyed) {
          enemyBotTower2Destroyed = true;
          destroyed = true;
        }
      }

      //mid
      if ((minion.getX() >= 400 && minion.getX() <= 2000 && minion.getY() >= 2000 && minion.getY() <= 3600)
          || (minion.getX() >= 2000 && minion.getX() <= 3600 && minion.getY() >= 400 && minion.getY() <= 2000)) {
        if (minion.getY() < Vertex.ENEMY_MID_TOWER1.y - 20 && !enemyMidTower1Destroyed) {
          enemyMidTower1Destroyed = true;
          destroyed = true;
        }
        if (minion.getY() < Vertex.ENEMY_MID_TOWER2.y - 20 && !enemyMidTower2Destroyed) {
          enemyMidTower2Destroyed = true;
          destroyed = true;
        }
      }
    }
    if (destroyed) {
      log("ENEMY TOWER DESTROYED (%s|%s|%s|%s|%s|%s)", enemyBotTower1Destroyed, enemyBotTower2Destroyed,
          enemyMidTower1Destroyed, enemyMidTower2Destroyed, enemyTopTower1Destroyed, enemyTopTower2Destroyed);
    }
  }

  private void changeLine() {
    //2 towers
    if (!enemyTopTower1Destroyed && !enemyTopTower2Destroyed) {
      currentPath = TOP_LANE_PATH;
      currentLane = Lane.TOP;
    } else if (!enemyMidTower1Destroyed && !enemyMidTower2Destroyed) {
      currentLane = Lane.MID;
      currentPath = MID_LANE_PATH;
    } else if (!enemyBotTower1Destroyed && !enemyBotTower2Destroyed) {
      currentLane = Lane.BOT;
      currentPath = BOTTOM_LANE_PATH;
    } else if (!enemyTopTower1Destroyed || !enemyTopTower2Destroyed) {//1 tower
      currentLane = Lane.TOP;
      currentPath = TOP_LANE_PATH;
    } else if (!enemyMidTower1Destroyed || !enemyMidTower2Destroyed) {
      currentLane = Lane.MID;
      currentPath = MID_LANE_PATH;
    } else if (!enemyBotTower1Destroyed || !enemyBotTower2Destroyed) {
      currentLane = Lane.BOT;
      currentPath = BOTTOM_LANE_PATH;
    } else {
      currentLane = Lane.MID;
      currentPath = MID_LANE_PATH;
    }
    log("CHANGE LANE TO %s", currentLane.name());
    //TODO push/def!
  }

  private Faction oppositeFaction() {
    return self.getFaction() == Faction.ACADEMY ? Faction.RENEGADES : Faction.ACADEMY;
  }

  private void log(String format, Object...args) {
//    System.out.println(String.format(format, args) + " --- " + (world == null ? "init" : world.getTickIndex()));
  }
}
