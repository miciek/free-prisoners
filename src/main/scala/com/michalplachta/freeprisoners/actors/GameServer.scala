package com.michalplachta.freeprisoners.actors

import akka.actor.Actor
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.actors.GameServer._

class GameServer extends Actor {
  private var savedDecisions = Map.empty[Prisoner, Decision]

  def receive: Receive = {
    case SaveDecision(prisoner, decision) =>
      savedDecisions += (prisoner -> decision)

    case GetSavedDecision(prisoner) =>
      sender ! savedDecisions.get(prisoner)

    case ClearSavedDecision(prisoner) =>
      savedDecisions -= prisoner
  }
}

object GameServer {
  sealed trait ServerProtocol[A]
  final case class SaveDecision(prisoner: Prisoner, decision: Decision)
      extends ServerProtocol[Unit]

  final case class GetSavedDecision(prisoner: Prisoner)
      extends ServerProtocol[Option[Decision]]

  final case class ClearSavedDecision(prisoner: Prisoner)
      extends ServerProtocol[Unit]
}
