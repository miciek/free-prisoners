package com.michalplachta.freeprisoners.freestyle.testhandlers

import java.util.UUID

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.freestyle.algebras.Game
import com.michalplachta.freeprisoners.states.GameState.GameStateA

trait GameTestHandler {
  implicit val gameTestHandler = new Game.Handler[GameStateA] {
    override def getGameHandle(player: PrisonersDilemma.Prisoner,
                               opponent: PrisonersDilemma.Prisoner) = {
      State { state =>
        (state, UUID.randomUUID())
      }
    }

    override def sendDecision(gameHandle: UUID,
                              player: PrisonersDilemma.Prisoner,
                              decision: PrisonersDilemma.Decision) = {
      State { state =>
        (state.copy(decisions = state.decisions + (player -> decision)), ())
      }
    }

    override def getOpponentDecision(gameHandle: UUID,
                                     opponent: PrisonersDilemma.Prisoner) = {
      State { state =>
        if (state.delayInCalls <= 0) {
          (state, state.decisions.get(opponent))
        } else {
          (state.copy(delayInCalls = state.delayInCalls - 1), None)
        }
      }
    }
  }
}
