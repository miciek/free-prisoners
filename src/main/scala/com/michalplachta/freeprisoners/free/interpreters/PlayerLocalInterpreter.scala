package com.michalplachta.freeprisoners.free.interpreters

import cats.free.Free
import cats.~>
import com.michalplachta.freeprisoners.free.algebras.DecisionRegistryOps.DecisionRegistry
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.{
  GetPrisonerDecision,
  GiveVerdict,
  MeetPrisoner,
  Player
}

class PlayerLocalInterpreter[S[_]](implicit gameOps: DecisionRegistry.Ops[S],
                                   playerOps: Player.Ops[S])
    extends (Player ~> Free[S, ?]) {
  import gameOps._
  import playerOps._
  /*_*/
  override def apply[A](fa: Player[A]) = fa match {
    case MeetPrisoner(introduction) =>
      meetPrisoner(introduction)
    case GetPrisonerDecision(prisoner, otherPrisoner) =>
      for {
        decision <- getPlayerDecision(prisoner, otherPrisoner)
        _ <- registerDecision(prisoner, decision)
      } yield decision
    case GiveVerdict(prisoner, verdict) =>
      giveVerdict(prisoner, verdict)
  }
}
