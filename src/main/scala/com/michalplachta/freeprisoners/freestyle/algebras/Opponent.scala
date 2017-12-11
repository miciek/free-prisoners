package com.michalplachta.freeprisoners.freestyle.algebras

import com.michalplachta.freeprisoners.PrisonersDilemma._
import freestyle._

@free trait Opponent {
  def meetOpponent(player: Prisoner): FS[Option[Prisoner]]

  def getOpponentDecision(prisoner: Prisoner,
                          otherPrisoner: Prisoner): FS[Decision]
}
