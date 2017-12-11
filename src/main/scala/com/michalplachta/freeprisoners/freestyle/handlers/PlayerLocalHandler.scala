package com.michalplachta.freeprisoners.freestyle.handlers

import com.michalplachta.freeprisoners.PrisonersDilemma.{Prisoner, Verdict}
import com.michalplachta.freeprisoners.freestyle.algebras.{
  DecisionRegistry,
  Player
}
import freestyle._

/*_*/
class PlayerLocalHandler[S[_]](implicit G: DecisionRegistry[S], P: Player[S])
    extends Player.Handler[FreeS[S, ?]] {
  override def meetPrisoner(introduction: String) =
    P.meetPrisoner(introduction)

  override def getPlayerDecision(prisoner: Prisoner, otherPrisoner: Prisoner) =
    for {
      decision <- P.getPlayerDecision(prisoner, otherPrisoner)
      _ <- G.registerDecision(prisoner, decision)
    } yield decision

  override def giveVerdict(prisoner: Prisoner, verdict: Verdict) =
    P.giveVerdict(prisoner, verdict)
}
