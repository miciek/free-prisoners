package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}

object GameOps {
  sealed trait Game[A]
  final case class GetGameHandle(player: Prisoner, opponent: Prisoner)
      extends Game[String]
  final case class SendDecision(gameHandle: String,
                                player: Prisoner,
                                decision: Decision)
      extends Game[Unit]
  final case class GetOpponentDecision(gameHandle: String, opponent: Prisoner)
      extends Game[Option[Decision]]

  object Game {
    class Ops[S[_]](implicit s: Game :<: S) {
      def getGameHandle(player: Prisoner, opponent: Prisoner): Free[S, String] =
        Free.liftF(s.inj(GetGameHandle(player, opponent)))

      def sendDecision(gameHandle: String,
                       player: Prisoner,
                       decision: Decision): Free[S, Unit] =
        Free.liftF(s.inj(SendDecision(gameHandle, player, decision)))

      def getOpponentDecision(gameHandle: String,
                              opponent: Prisoner): Free[S, Option[Decision]] =
        Free.liftF(s.inj(GetOpponentDecision(gameHandle, opponent)))
    }

    object Ops {
      def apply[S[_]](implicit s: Game :<: S): Ops[S] = new Ops[S]
    }
  }
}
