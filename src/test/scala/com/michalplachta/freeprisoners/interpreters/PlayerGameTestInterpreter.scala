package com.michalplachta.freeprisoners.interpreters

import cats.data.{EitherK, State}
import cats.~>
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.interpreters.GameTestInterpreter.{
  GameState,
  GameStateA
}
import com.michalplachta.freeprisoners.interpreters.PlayerGameTestInterpreter.{
  PlayerGame,
  PlayerGameState,
  PlayerGameStateA
}
import com.michalplachta.freeprisoners.interpreters.PlayerTestInterpreter.{
  PlayerState,
  PlayerStateA
}

class PlayerGameTestInterpreter extends (PlayerGame ~> PlayerGameStateA) {
  private val playerInterpreter: Player ~> PlayerGameStateA =
    (new PlayerTestInterpreter)
      .andThen(new (PlayerStateA ~> PlayerGameStateA) {
        def apply[A](playerState: PlayerStateA[A]): PlayerGameStateA[A] =
          playerState.transformS[PlayerGameState](
            _.playerState,
            (state, playerState) => state.copy(playerState = playerState))
      })
  private val gameInterpreter: Game ~> PlayerGameStateA =
    (new GameTestInterpreter)
      .andThen(new (GameStateA ~> PlayerGameStateA) {
        def apply[A](gameState: GameStateA[A]): PlayerGameStateA[A] =
          gameState.transformS[PlayerGameState](
            _.gameState,
            (state, gameState) => state.copy(gameState = gameState))
      })

  private val interpreter = playerInterpreter or gameInterpreter

  def apply[A](playerGame: PlayerGame[A]) = interpreter.apply(playerGame)
}

object PlayerGameTestInterpreter {
  type PlayerGame[A] = EitherK[Player, Game, A]

  final case class PlayerGameState(playerState: PlayerState,
                                   gameState: GameState)
  type PlayerGameStateA[A] = State[PlayerGameState, A]
}
