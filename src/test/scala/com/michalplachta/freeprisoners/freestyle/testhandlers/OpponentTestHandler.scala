package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma
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
    def meetOpponent = {
      State { state =>
        val prisoner = Prisoner("Test")
        (state.copy(
           opponents = state.opponents + (prisoner -> Strategy(_ => Guilty))),
         prisoner)
      }
    }

    def getOpponentDecision(prisoner: PrisonersDilemma.Prisoner,
                            otherPrisoner: PrisonersDilemma.Prisoner) = {
      State.inspect(
        _.opponents.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence))
    }
  }
}
