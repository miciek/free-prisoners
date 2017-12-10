package com.michalplachta.freeprisoners.states

import cats.data.State

final case class PlayerOpponentState(playerState: PlayerState,
                                     opponentState: OpponentState)

object PlayerOpponentState {
  type PlayerOpponentStateA[A] = State[PlayerOpponentState, A]
}
