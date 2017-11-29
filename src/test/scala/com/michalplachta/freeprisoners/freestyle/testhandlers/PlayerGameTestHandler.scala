package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.~>
import com.michalplachta.freeprisoners.freestyle.algebras.{Game, Player}
import com.michalplachta.freeprisoners.states.GameState.GameStateA
import com.michalplachta.freeprisoners.states.PlayerGameState.PlayerGameStateA
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

trait PlayerGameTestHandler extends PlayerTestHandler with GameTestHandler {
  implicit val playerHandler: Player.Op ~> PlayerGameStateA =
    playerTestHandler.andThen(new (PlayerStateA ~> PlayerGameStateA) {
      override def apply[A](
          playerStateA: PlayerStateA[A]): PlayerGameStateA[A] =
        playerStateA.transformS(
          _.playerState,
          (state, playerState) => state.copy(playerState = playerState))
    })

  implicit val gameHandler: Game.Op ~> PlayerGameStateA =
    gameTestHandler.andThen(new (GameStateA ~> PlayerGameStateA) {
      override def apply[A](gameStateA: GameStateA[A]): PlayerGameStateA[A] =
        gameStateA.transformS(
          _.gameState,
          (state, gameState) => state.copy(gameState = gameState))
    })
}
