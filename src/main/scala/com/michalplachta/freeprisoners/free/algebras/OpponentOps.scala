package com.michalplachta.freeprisoners.free.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma._

object OpponentOps {
  sealed trait Opponent[A]
  final case class MeetOpponent(player: Prisoner)
      extends Opponent[Option[Prisoner]]
  final case class GetOpponentDecision(player: Prisoner, opponent: Prisoner)
      extends Opponent[Decision]

  object Opponent {
    class Ops[S[_]](implicit s: Opponent :<: S) {
      def meetOpponent(player: Prisoner): Free[S, Option[Prisoner]] =
        Free.inject(MeetOpponent(player))

      def getOpponentDecision(player: Prisoner,
                              opponent: Prisoner): Free[S, Decision] =
        Free.inject(GetOpponentDecision(player, opponent))
    }
  }
}
