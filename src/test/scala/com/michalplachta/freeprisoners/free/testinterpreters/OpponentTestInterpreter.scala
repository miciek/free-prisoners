package com.michalplachta.freeprisoners.free.testinterpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.{
  GetOpponentDecision,
  MeetOpponent,
  Opponent
}
import com.michalplachta.freeprisoners.states.OpponentState.OpponentStateA

class OpponentTestInterpreter extends (Opponent ~> OpponentStateA) {
  /*_*/
  override def apply[A](opponent: Opponent[A]) = opponent match {
    case MeetOpponent() =>
      State { state =>
        val prisoner = Prisoner("Test")
        (state.copy(
           opponents = state.opponents + (prisoner -> Strategy(_ => Guilty))),
         prisoner)
      }

    case GetOpponentDecision(prisoner, otherPrisoner) =>
      State.inspect(
        _.opponents.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence))
  }
}
