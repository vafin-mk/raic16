import model.*;

public final class MyStrategy implements Strategy {

  private Graph navGraph;
  private Wizard self;
  private World world;
  private Game game;
  private Move move;

  @Override
  public void move(Wizard self, World world, Game game, Move move) {
    updateData(self, world, game, move);
    move.setSpeed(game.getWizardForwardSpeed());
    move.setStrafeSpeed(game.getWizardStrafeSpeed());
    move.setTurn(game.getWizardMaxTurnAngle());
    move.setAction(ActionType.MAGIC_MISSILE);
  }

  private void updateData(Wizard self, World world, Game game, Move move) {
    this.self = self;
    this.world = world;
    this.game = game;
    this.move = move;
  }


  //INITIAL STAGE
  MyStrategy() {
    navGraph = GraphFactory.buildNavigationGraph();
  }
}
