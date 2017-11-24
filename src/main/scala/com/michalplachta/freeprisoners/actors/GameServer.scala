package com.michalplachta.freeprisoners.actors

import java.util.UUID

import akka.actor.Actor
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.actors.GameServer._

class GameServer extends Actor {
  private var createdGames = Map.empty[Prisoner, Game]
  private var savedDecisions = Map.empty[(String, Prisoner), Decision]

  def receive: Receive = {
    case GetGameId(player, opponent) =>
      val gameId: String =
        createdGames.get(player).filter(_.owner == opponent) match {
          case Some(existingGame) =>
            createdGames = createdGames.filterKeys(_ != player)
            existingGame.id
          case None =>
            val newGameId = UUID.randomUUID().toString
            createdGames += (opponent -> Game(newGameId, player, opponent))
            newGameId
        }
      sender ! gameId
    case SaveDecision(gameId, player, decision) =>
      savedDecisions += ((gameId, player) -> decision)
    case GetSavedDecision(gameId, player) =>
      sender ! savedDecisions.get((gameId, player))
  }
}

object GameServer {
  final case class Game(id: String, owner: Prisoner, opponent: Prisoner)

  sealed trait ServerProtocol[A]
  final case class GetGameId(player: Prisoner, opponent: Prisoner)
      extends ServerProtocol[String]
  final case class SaveDecision(gameId: String,
                                player: Prisoner,
                                decision: Decision)
      extends ServerProtocol[Unit]
  final case class GetSavedDecision(gameId: String, player: Prisoner)
      extends ServerProtocol[Option[Decision]]
}
