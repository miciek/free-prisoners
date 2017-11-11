package com.michalplachta.freeprisoners.interpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.algebras.GameOps.{
  Game,
  GetOpponentDecision,
  SendDecision
}
import com.michalplachta.freeprisoners.interpreters.GameTestInterpreter.DecisionState

class GameTestInterpreter extends (Game ~> DecisionState) {
  def apply[A](game: Game[A]): DecisionState[A] = game match {
    case SendDecision(player, opponent, decision) =>
      State { state =>
        (state + (player -> decision), ())
      }
    case GetOpponentDecision(player, opponent, waitTime) =>
      State { state =>
        (state, state.get(opponent))
      }
  }
}

object GameTestInterpreter {
  type DecisionState[A] = State[Map[Prisoner, Decision], A]
}
