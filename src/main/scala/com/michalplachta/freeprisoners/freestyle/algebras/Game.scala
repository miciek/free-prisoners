package com.michalplachta.freeprisoners.freestyle.algebras

import java.util.UUID

import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import freestyle.free

@free trait Game {
  def getGameHandle(player: Prisoner, opponent: Prisoner): FS[UUID]

  def sendDecision(gameHandle: UUID,
                   player: Prisoner,
                   decision: Decision): FS[Unit]

  def getOpponentDecision(gameHandle: UUID,
                          opponent: Prisoner): FS[Option[Decision]]
}
