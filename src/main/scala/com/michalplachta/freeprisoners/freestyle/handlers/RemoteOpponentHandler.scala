package com.michalplachta.freeprisoners.freestyle.handlers

import com.michalplachta.freeprisoners.PrisonersDilemma.{Prisoner, Silence}
import com.michalplachta.freeprisoners.freestyle.algebras.{
  DecisionRegistry,
  Matchmaking,
  Opponent,
  Timing
}
import com.michalplachta.freeprisoners.freestyle.programs.Multiplayer
import freestyle.FreeS

class RemoteOpponentHandler[S[_]: Matchmaking: DecisionRegistry: Timing]
    extends (Opponent.Handler[FreeS[S, ?]]) {
  override def meetOpponent(player: Prisoner) =
    Multiplayer.findOpponent[S](player)

  override def getOpponentDecision(player: Prisoner, opponent: Prisoner) =
    Multiplayer.getRemoteOpponentDecision[S](opponent).map(_.getOrElse(Silence))
}
