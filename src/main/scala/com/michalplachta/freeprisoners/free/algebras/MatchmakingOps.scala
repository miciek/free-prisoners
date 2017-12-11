package com.michalplachta.freeprisoners.free.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking.WaitingPlayer

object MatchmakingOps {
  sealed trait Matchmaking[A]
  final case class RegisterAsWaiting(player: Prisoner) extends Matchmaking[Unit]
  final case class UnregisterWaiting(player: Prisoner) extends Matchmaking[Unit]
  final case class GetWaitingPlayers() extends Matchmaking[List[WaitingPlayer]]
  final case class JoinWaitingPlayer(player: Prisoner,
                                     waitingPlayer: WaitingPlayer)
      extends Matchmaking[Option[Prisoner]]
  final case class CheckIfOpponentJoined(player: Prisoner)
      extends Matchmaking[Option[Prisoner]]

  object Matchmaking {
    class Ops[S[_]](implicit s: Matchmaking :<: S) {
      def registerAsWaiting(player: Prisoner): Free[S, Unit] =
        Free.inject(RegisterAsWaiting(player))

      def unregisterWaiting(player: Prisoner): Free[S, Unit] =
        Free.inject(UnregisterWaiting(player))

      def getWaitingPlayers(): Free[S, List[WaitingPlayer]] =
        Free.inject(GetWaitingPlayers())

      def joinWaitingPlayer(
          player: Prisoner,
          waitingPlayer: WaitingPlayer): Free[S, Option[Prisoner]] =
        Free.inject(JoinWaitingPlayer(player, waitingPlayer))

      def checkIfOpponentJoined(player: Prisoner): Free[S, Option[Prisoner]] =
        Free.inject(CheckIfOpponentJoined(player))
    }

    final case class WaitingPlayer(prisoner: Prisoner)
  }
}
