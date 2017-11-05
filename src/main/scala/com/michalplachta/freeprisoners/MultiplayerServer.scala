package com.michalplachta.freeprisoners

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.michalplachta.freeprisoners.PrisonersDilemma.Decision
import com.typesafe.config.ConfigFactory

object MultiplayerServer extends App {
  sealed trait ServerProtocol[A]
  final case class RegisterPlayer(name: String) extends ServerProtocol[Unit]
  final case class GetPlayerOpponent(name: String)
      extends ServerProtocol[String]
  final case class RegisterDecision(playerName: String, decision: Decision)
      extends ServerProtocol[Unit]
  final case class GetRegisteredDecision(playerName: String)
      extends ServerProtocol[Decision]

  private final case class PlayingPrisoner(playerName: String,
                                           opponentName: String,
                                           decision: Option[Decision])

  private class Server extends Actor with ActorLogging {
    private var waitingPlayers = Set.empty[String]
    private var playingPrisoners = Map.empty[String, PlayingPrisoner]

    def receive: Receive = {
      case RegisterPlayer(name) =>
        waitingPlayers += name
        playingPrisoners = playingPrisoners.filterKeys(_ != name)
        log.info(
          s"Registered $name. Waiting: ${waitingPlayers.mkString(", ")}. Playing: $playingPrisoners")

      case GetPlayerOpponent(name) =>
        if (playingPrisoners.contains(name)) {
          playingPrisoners.get(name).foreach(sender ! _.opponentName)
        } else if (waitingPlayers.contains(name) && waitingPlayers.size > 1) {
          val otherPrisoners = waitingPlayers.filterNot(_ == name)
          val opponentName = otherPrisoners.head
          val opponentPlayer = PlayingPrisoner(opponentName, name, None)
          val prisonerPlayer = PlayingPrisoner(name, opponentName, None)
          playingPrisoners ++= Map(name -> prisonerPlayer,
                                   opponentName -> opponentPlayer)
          waitingPlayers = otherPrisoners.tail
          sender ! opponentName
        }

      case RegisterDecision(name, decision) =>
        if (playingPrisoners.contains(name)) {
          val updatedPrisoner = playingPrisoners
            .get(name)
            .map(name -> _.copy(decision = Some(decision)))
            .toMap
          playingPrisoners ++= updatedPrisoner
          log.info(s"Received decision of $name: $decision")
        } else {
          log.info(s"Received decision for unknown player $name")
        }

      case GetRegisteredDecision(name) =>
        playingPrisoners
          .get(name)
          .flatMap(_.decision)
          .foreach(sender ! _)
    }
  }

  private val system =
    ActorSystem("prisonersDilemma", ConfigFactory.load("server"))
  system.actorOf(Props[Server], "server")
}
