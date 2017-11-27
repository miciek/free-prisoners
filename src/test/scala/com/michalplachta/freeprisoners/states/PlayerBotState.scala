package com.michalplachta.freeprisoners.states

import cats.data.State

final case class PlayerBotState(playerState: PlayerState, botState: BotState)

object PlayerBotState {
  type PlayerBotStateA[A] = State[PlayerBotState, A]
}
