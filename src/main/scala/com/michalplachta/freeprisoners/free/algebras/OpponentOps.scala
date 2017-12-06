package com.michalplachta.freeprisoners.free.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma._

object OpponentOps {
  sealed trait Opponent[A]
  final case class MeetOpponent() extends Opponent[Prisoner]
  final case class GetOpponentDecision(prisoner: Prisoner,
                                       otherPrisoner: Prisoner)
      extends Opponent[Decision]

  object Opponent {
    class Ops[S[_]](implicit s: Opponent :<: S) {
      def meetOpponent(): Free[S, Prisoner] =
        Free.inject(MeetOpponent())

      def getOpponentDecision(prisoner: Prisoner,
                              otherPrisoner: Prisoner): Free[S, Decision] =
        Free.inject(GetOpponentDecision(prisoner, otherPrisoner))
    }
  }
}
