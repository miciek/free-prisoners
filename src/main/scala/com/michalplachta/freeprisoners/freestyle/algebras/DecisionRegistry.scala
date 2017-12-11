package com.michalplachta.freeprisoners.freestyle.algebras

import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import freestyle.free

@free trait DecisionRegistry {
  def registerDecision(prisoner: Prisoner, decision: Decision): FS[Unit]

  def getRegisteredDecision(prisoner: Prisoner): FS[Option[Decision]]

  def clearRegisteredDecision(prisoner: Prisoner): FS[Unit]
}
