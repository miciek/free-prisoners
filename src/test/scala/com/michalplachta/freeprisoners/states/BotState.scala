package com.michalplachta.freeprisoners.states

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.{Prisoner, Strategy}

final case class BotState(bots: Map[Prisoner, Strategy])

object BotHandler {
  type BotStateA[A] = State[BotState, A]
}
