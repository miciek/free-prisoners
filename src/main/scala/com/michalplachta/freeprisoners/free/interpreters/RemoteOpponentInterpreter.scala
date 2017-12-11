package com.michalplachta.freeprisoners.free.interpreters

import cats.free.Free
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.Silence
import com.michalplachta.freeprisoners.free.algebras.DecisionRegistryOps.DecisionRegistry
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.{
  GetOpponentDecision,
  MeetOpponent,
  Opponent
}
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.free.programs.Multiplayer

class RemoteOpponentInterpreter[
    S[_]: Matchmaking.Ops: DecisionRegistry.Ops: Timing.Ops]
    extends (Opponent ~> Free[S, ?]) {
  /*_*/
  override def apply[A](fa: Opponent[A]) = fa match {
    case MeetOpponent(player) =>
      Multiplayer.findOpponent[S](player)
    case GetOpponentDecision(_, opponent) =>
      Multiplayer.getRemoteOpponentDecision(opponent).map(_.getOrElse(Silence))
  }
}
