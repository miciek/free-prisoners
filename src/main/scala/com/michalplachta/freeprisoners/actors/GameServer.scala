package com.michalplachta.freeprisoners.actors

import akka.actor.Actor
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.actors.GameServer.{
  GetSavedDecision,
  SaveDecision
}

class GameServer extends Actor {
  private var savedDecisions = Map.empty[(Prisoner, Prisoner), Decision]

  def receive: Receive = {
    case SaveDecision(player, against, decision) =>
      savedDecisions += ((player, against) -> decision)
    case GetSavedDecision(player, against) =>
      sender ! savedDecisions.get((player, against))
  }
}

object GameServer {
  sealed trait ServerProtocol[A]
  final case class SaveDecision(player: Prisoner,
                                against: Prisoner,
                                decision: Decision)
      extends ServerProtocol[Unit]
  final case class GetSavedDecision(player: Prisoner, against: Prisoner)
      extends ServerProtocol[Option[Decision]]
}
