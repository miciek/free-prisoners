package com.michalplachta.freeprisoners.freestyle.algebras

import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import freestyle.free

@free trait Matchmaking {
  def registerAsWaiting(player: Prisoner): FS[Unit]

  def unregisterWaiting(player: Prisoner): FS[Unit]

  def getWaitingPlayers: FS[List[WaitingPlayer]]

  def joinWaitingPlayer(player: Prisoner,
                        waitingPlayer: WaitingPlayer): FS[Option[Prisoner]]

  def checkIfOpponentJoined(player: Prisoner): FS[Option[Prisoner]]
}

final case class WaitingPlayer(prisoner: Prisoner)
