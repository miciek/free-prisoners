package com.michalplachta.freeprisoners.states

import cats.data.State
import com.michalplachta.freeprisoners.freestyle.handlers.BotHandler.BotState

final case class PlayerBotState(playerState: PlayerState, botState: BotState)

object PlayerBotState {
  type PlayerBotStateA[A] = State[PlayerBotState, A]
}
