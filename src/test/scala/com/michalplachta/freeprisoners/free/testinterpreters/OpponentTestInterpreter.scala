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
  override def apply[A](opponentA: Opponent[A]) = opponentA match {
    case MeetOpponent(_) =>
      State { state =>
        val prisoner = Prisoner("Test")
        (state.copy(
           opponents = state.opponents + (prisoner -> Strategy(_ => Guilty))),
         Some(prisoner))
      }

    case GetOpponentDecision(player, opponent) =>
      State.inspect(
        _.opponents.get(opponent).map(_.f(player)).getOrElse(Silence))
  }
}
