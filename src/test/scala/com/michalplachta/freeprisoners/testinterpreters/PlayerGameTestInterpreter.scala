package com.michalplachta.freeprisoners.testinterpreters

import cats.data.{EitherK, State}
import cats.~>
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.testinterpreters.GameTestInterpreter.{
  GameState,
  GameStateA
}
import com.michalplachta.freeprisoners.testinterpreters.PlayerGameTestInterpreter.{
  PlayerGame,
  PlayerGame0,
  PlayerGameState,
  PlayerGameStateA
}
import com.michalplachta.freeprisoners.testinterpreters.PlayerTestInterpreter.{
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
  private val interpreter0: PlayerGame0 ~> PlayerGameStateA =
    playerInterpreter or gameInterpreter
  private val interpreter = new TimingTestInterpreter[PlayerGameStateA] or interpreter0

  def apply[A](playerGame: PlayerGame[A]) = interpreter.apply(playerGame)
}

object PlayerGameTestInterpreter {
  type PlayerGame0[A] = EitherK[Player, Game, A]
  type PlayerGame[A] = EitherK[Timing, PlayerGame0, A]

  final case class PlayerGameState(playerState: PlayerState,
                                   gameState: GameState)
  type PlayerGameStateA[A] = State[PlayerGameState, A]
}
