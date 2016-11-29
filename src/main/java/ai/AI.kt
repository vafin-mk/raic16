package ai

import extensions.canFight
import extensions.toPoint
import model.Game
import model.Move
import model.Wizard
import model.World
import pathfinding.Lane
import java.nio.file.Files.move



class AI {

  fun decision() {
    fightDecision()
    turnDecision()
    moveDecision()
    if (game.isSkillsEnabled && self.level > self.getSkills().size) {
      learnDecision(self.getSkills().size)
    }
  }

  private fun fightDecision() {
    if (!self.canFight()) {
      return
    }

  }

  private fun turnDecision() {
    move.turn = 0.2
  }

  private fun moveDecision() {
    val target = if (self.life < 50) lane.previousPoint(self.toPoint()) else lane.nextPoint(self.toPoint())
    val dist = target.dist(self.toPoint())
    val angle = self.getAngleTo(target.x, target.y)
    move.speed = dist * StrictMath.cos(angle)
    move.strafeSpeed = dist * StrictMath.sin(angle)
  }

  private fun learnDecision(level: Int) {

  }

  fun updateInfo(self: Wizard, world: World, game: Game, move: Move) {
    updateVars(self, world, game, move)
  }

  private fun updateVars(self: Wizard, world: World, game: Game, move: Move) {
    this.self = self
    this.world = world
    this.game = game
    this.move = move
    if (world.tickIndex == 0) {
      initStrategy()
    }
  }

  private fun initStrategy() {
    when(self.id.toInt()) {
      1, 2, 6, 7 -> lane = Lane.TOP
      4, 5, 9, 10 -> lane = Lane.BOTTOM
      3, 8 -> lane = Lane.MID
      else -> lane = Lane.MID
    }
  }

  lateinit var self : Wizard
  lateinit var world : World
  lateinit var game : Game
  lateinit var move : Move
  lateinit var lane: Lane
}