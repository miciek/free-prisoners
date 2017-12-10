package com.michalplachta.freeprisoners.states

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.{Prisoner, Strategy}

final case class OpponentState(opponents: Map[Prisoner, Strategy])

object OpponentState {
  type OpponentStateA[A] = State[OpponentState, A]
}
