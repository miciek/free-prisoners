package com.michalplachta.freeprisoners.freestyle.algebras

import com.michalplachta.freeprisoners.PrisonersDilemma._
import freestyle._

@free trait Bot {
  def createBot(name: String, strategy: Strategy): FS[Prisoner]

  def getDecision(prisoner: Prisoner, otherPrisoner: Prisoner): FS[Decision]
}
