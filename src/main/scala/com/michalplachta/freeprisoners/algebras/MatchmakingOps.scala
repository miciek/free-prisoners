package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner

import scala.concurrent.duration.FiniteDuration

object MatchmakingOps {
  sealed trait Matchmaking[A]
  final case class GetWaitingOpponents() extends Matchmaking[Seq[Int]]
  final case class JoinWaitingOpponent(waitingOpponentId: Int)
      extends Matchmaking[Option[Prisoner]]
  final case class WaitForOpponent(waitTime: FiniteDuration)
      extends Matchmaking[Option[Prisoner]]

  object Matchmaking {
    class Ops[S[_]](implicit s: Matchmaking :<: S) {
      def getWaitingOpponents(): Free[S, Seq[Int]] =
        Free.liftF(s.inj(GetWaitingOpponents()))
      def joinWaitingOpponent(
          waitingOpponentId: Int): Free[S, Option[Prisoner]] =
        Free.liftF(s.inj(JoinWaitingOpponent(waitingOpponentId)))
      def waitForOpponent(waitTime: FiniteDuration): Free[S, Option[Prisoner]] =
        Free.liftF(s.inj(WaitForOpponent(waitTime)))
    }

    object Ops {
      def apply[S[_]](implicit s: Matchmaking :<: S): Ops[S] = new Ops[S]
    }
  }
}
