package com.michalplachta.freeprisoners.states

import cats.data.State

final case class PlayerGameState(playerState: PlayerState, gameState: GameState)

object PlayerGameState {
  type PlayerGameStateA[A] = State[PlayerGameState, A]
}
