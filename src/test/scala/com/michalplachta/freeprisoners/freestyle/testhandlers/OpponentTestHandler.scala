package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.freestyle.algebras.Opponent
import com.michalplachta.freeprisoners.states.OpponentState.OpponentStateA

trait OpponentTestHandler {
  implicit val opponentTestHandler = new Opponent.Handler[OpponentStateA] {
    def meetOpponent(player: Prisoner) = {
      State { state =>
        val prisoner = Prisoner("Test")
        (state.copy(
           opponents = state.opponents + (prisoner -> Strategy(_ => Guilty))),
         Some(prisoner))
      }
    }

    def getOpponentDecision(player: Prisoner, opponent: Prisoner) = {
      State.inspect(
        _.opponents.get(opponent).map(_.f(player)).getOrElse(Silence))
    }
  }
}
