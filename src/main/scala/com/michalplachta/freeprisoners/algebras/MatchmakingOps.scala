package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner

import scala.concurrent.duration.FiniteDuration

object MatchmakingOps {
  sealed trait Matchmaking[A]

  object Matchmaking {
    sealed trait MatchmakingResult
    final case class OpponentFound(opponent: Prisoner) extends MatchmakingResult
    final case object OpponentNotFound extends MatchmakingResult

    class Ops[S[_]](implicit s: Matchmaking :<: S) {
      def waitForOpponent(
          prisoner: Prisoner,
          waitTime: FiniteDuration): Free[S, MatchmakingResult] =
        ???
      def joinWaitingOpponent(
          prisoner: Prisoner,
          waitTime: FiniteDuration): Free[S, MatchmakingResult] =
        ???
    }

    object Ops {
      def apply[S[_]](implicit s: Matchmaking :<: S): Ops[S] = new Ops[S]
    }
  }
}
