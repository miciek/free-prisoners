package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}

import scala.concurrent.duration.FiniteDuration

object MatchOps {
  sealed trait Match[A]

  object Match {
    class Ops[S[_]](implicit s: Match :<: S) {
      def sendDecision(prisoner: Prisoner,
                       opponent: Prisoner,
                       decision: Decision): Free[S, Unit] = ???
      def getOpponentDecision(
          prisoner: Prisoner,
          opponent: Prisoner,
          waitTime: FiniteDuration): Free[S, Option[Decision]] =
        ???
    }

    object Ops {
      def apply[S[_]](implicit s: Match :<: S): Ops[S] = new Ops[S]
    }
  }
}
