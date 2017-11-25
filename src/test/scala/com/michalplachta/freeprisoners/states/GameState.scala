package com.michalplachta.freeprisoners.states

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}

final case class GameState(decisions: Map[Prisoner, Decision],
                           delayInCalls: Int = 0)

object GameState {
  type GameStateA[A] = State[GameState, A]
}
