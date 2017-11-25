package com.michalplachta.freeprisoners.free.testinterpreters

import java.util.UUID

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.free.algebras.GameOps.{
  Game,
  GetGameHandle,
  GetOpponentDecision,
  SendDecision
}
import com.michalplachta.freeprisoners.states.GameState.GameStateA

class GameTestInterpreter extends (Game ~> GameStateA) {
  def apply[A](game: Game[A]): GameStateA[A] = game match {
    case GetGameHandle(_, _) =>
      State { state =>
        (state, UUID.randomUUID())
      }
    case SendDecision(_, player, decision) =>
      State { state =>
        (state.copy(decisions = state.decisions + (player -> decision)), ())
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
