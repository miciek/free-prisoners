package com.michalplachta.freeprisoners.free.testinterpreters

import cats.data.EitherK
import cats.~>
import com.michalplachta.freeprisoners.free.algebras.GameOps.Game
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.free.testinterpreters.PlayerGameTestInterpreter.{
  PlayerGame,
  PlayerGame0
}
import com.michalplachta.freeprisoners.states.GameState.GameStateA
import com.michalplachta.freeprisoners.states.PlayerGameState
import com.michalplachta.freeprisoners.states.PlayerGameState.PlayerGameStateA
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

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
}
