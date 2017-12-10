package com.michalplachta.freeprisoners.freestyle.algebras

import com.michalplachta.freeprisoners.PrisonersDilemma._
import freestyle._

@free trait Opponent {
  def meetOpponent(): FS[Prisoner]

  def getOpponentDecision(prisoner: Prisoner,
                          otherPrisoner: Prisoner): FS[Decision]
}
