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
    case SendDecision(player, opponent, decision) =>
      State { state =>
        (state.copy(decisions = state.decisions + (player -> decision)), ())
      }
    case ClearPlayerDecisions(player) =>
      State { state =>
        (state.copy(decisions = state.decisions.filterKeys(_ != player)), ())
      }
    case GetOpponentDecision(player, opponent, waitTime) =>
      State { state =>
        (state, state.decisions.get(opponent))
      }
  }
}

object GameTestInterpreter {
  final case class GameState(decisions: Map[Prisoner, Decision])
  type GameStateA[A] = State[GameState, A]
}
