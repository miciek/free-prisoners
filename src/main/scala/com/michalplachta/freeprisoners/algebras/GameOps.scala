package com.michalplachta.freeprisoners.algebras

import java.util.UUID

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}

object GameOps {
  sealed trait Game[A]
  final case class GetGameHandle(player: Prisoner, opponent: Prisoner)
      extends Game[UUID]
  final case class SendDecision(gameHandle: UUID,
                                player: Prisoner,
                                decision: Decision)
      extends Game[Unit]
  final case class GetOpponentDecision(gameHandle: UUID, opponent: Prisoner)
      extends Game[Option[Decision]]

  object Game {
    class Ops[S[_]](implicit s: Game :<: S) {
      def getGameHandle(player: Prisoner, opponent: Prisoner): Free[S, UUID] =
        Free.inject(GetGameHandle(player, opponent))

      def sendDecision(gameHandle: UUID,
                       player: Prisoner,
                       decision: Decision): Free[S, Unit] =
        Free.inject(SendDecision(gameHandle, player, decision))

      def getOpponentDecision(gameHandle: UUID,
                              opponent: Prisoner): Free[S, Option[Decision]] =
        Free.inject(GetOpponentDecision(gameHandle, opponent))
    }
  }
}
