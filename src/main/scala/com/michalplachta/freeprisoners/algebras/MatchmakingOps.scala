package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking.WaitingPlayer

object MatchmakingOps {
  sealed trait Matchmaking[A]
  final case class RegisterAsWaiting(player: Prisoner) extends Matchmaking[Unit]
  final case class UnregisterPlayer(player: Prisoner) extends Matchmaking[Unit]
  final case class GetWaitingPlayers() extends Matchmaking[Set[WaitingPlayer]]
  final case class JoinWaitingPlayer(player: Prisoner,
                                     waitingPlayer: WaitingPlayer)
      extends Matchmaking[Option[Prisoner]]
  final case class CheckIfOpponentJoined(player: Prisoner)
      extends Matchmaking[Option[Prisoner]]

  object Matchmaking {
    class Ops[S[_]](implicit s: Matchmaking :<: S) {
      def registerAsWaiting(player: Prisoner): Free[S, Unit] =
        Free.liftF(s.inj(RegisterAsWaiting(player)))

      def unregisterPlayer(player: Prisoner): Free[S, Unit] =
        Free.liftF(s.inj(UnregisterPlayer(player)))

      def getWaitingPlayers(): Free[S, Set[WaitingPlayer]] =
        Free.liftF(s.inj(GetWaitingPlayers()))

      def joinWaitingPlayer(
          player: Prisoner,
          waitingPlayer: WaitingPlayer): Free[S, Option[Prisoner]] =
        Free.liftF(s.inj(JoinWaitingPlayer(player, waitingPlayer)))

      def checkIfOpponentJoined(player: Prisoner): Free[S, Option[Prisoner]] =
        Free.liftF(s.inj(CheckIfOpponentJoined(player)))
    }

    object Ops {
      def apply[S[_]](implicit s: Matchmaking :<: S): Ops[S] = new Ops[S]
    }

    final case class WaitingPlayer(prisoner: Prisoner)
  }
}
