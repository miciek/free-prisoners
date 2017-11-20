package com.michalplachta.freeprisoners.testinterpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.algebras.GameOps.{
  ClearPlayerDecisions,
  Game,
  GetOpponentDecision,
  SendDecision
}
import com.michalplachta.freeprisoners.testinterpreters.GameTestInterpreter.GameStateA

class GameTestInterpreter extends (Game ~> GameStateA) {
  def apply[A](game: Game[A]): GameStateA[A] = game match {
    case SendDecision(player, _, decision) =>
      State { state =>
        (state.copy(decisions = state.decisions + (player -> decision)), ())
      }
    case ClearPlayerDecisions(player) =>
      State { state =>
        (state.copy(decisions = state.decisions.filterKeys(_ != player)), ())
      }
    case GetOpponentDecision(_, opponent) =>
      State { state =>
        if (state.delayInCalls <= 0) {
          (state, state.decisions.get(opponent))
        } else {
          (state.copy(delayInCalls = state.delayInCalls - 1), None)
        }
      }
  }
}

object GameTestInterpreter {
  final case class GameState(decisions: Map[Prisoner, Decision],
                             delayInCalls: Int = 0)
  type GameStateA[A] = State[GameState, A]
}
