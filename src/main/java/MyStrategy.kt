import java.util.*;
import java.lang.*;
import model.*;

class MyStrategy : Strategy {val ai = AI()
override fun move(self: Wizard, world: World, game: Game, move: Move) { ai.updateInfo(self, world, game, move); ai.decision();}}

class AI {

  fun decision() {

  }

  fun updateInfo(self: Wizard, world: World, game: Game, move: Move) {

  }

}


