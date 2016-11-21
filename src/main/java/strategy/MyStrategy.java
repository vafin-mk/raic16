package strategy;

import ai.AI;
import model.*;

public final class MyStrategy implements Strategy {

  private AI ai = new AI();
  @Override
  public void move(Wizard self, World world, Game game, Move move) {
    ai.updateWorldInfo(self, world, game, move);
    ai.makeDecision();
  }
}
