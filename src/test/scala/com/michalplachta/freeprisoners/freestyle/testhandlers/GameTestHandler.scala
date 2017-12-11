package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.freestyle.algebras.Game
import com.michalplachta.freeprisoners.states.GameState.GameStateA

trait GameTestHandler {
  implicit val gameTestHandler = new Game.Handler[GameStateA] {
    override def registerDecision(prisoner: Prisoner, decision: Decision) = {
      State { state =>
        (state.copy(decisions = state.decisions + (prisoner -> decision)), ())
      }
    }

    override def getRegisteredDecision(prisoner: Prisoner) = {
      State { state =>
        if (state.delayInCalls <= 0) {
          (state, state.decisions.get(prisoner))
        } else {
          (state.copy(delayInCalls = state.delayInCalls - 1), None)
        }
      }
    }

    override def clearRegisteredDecision(prisoner: Prisoner) = {
      State { state =>
        (state.copy(decisions = state.decisions - prisoner), ())
      }
    }
  }
}
