package com.michalplachta.freeprisoners.actors

import akka.actor.Actor
import com.michalplachta.freeprisoners.actors.MatchmakingServer._

class MatchmakingServer extends Actor {
  private var waitingList = Set.empty[String]
  private var matches = Set.empty[Set[String]]

  def receive: Receive = {
    case AddToWaitingList(name) =>
      waitingList = waitingList + name

    case RemoveFromMatchmaking(name) =>
      waitingList = waitingList - name
      matches = matches.filterNot(_.contains(name))

    case GetWaitingList() =>
      sender ! waitingList

    case RegisterMatch(playerA, playerB) =>
      matches += Set(playerA, playerB)

    case GetOpponentName(player) =>
      val game: Set[String] = matches.find(_.contains(player)).toSet.flatten
      sender ! game.filterNot(_ == player).headOption
  }
}

object MatchmakingServer {
  sealed trait ServerProtocol[A]
  final case class AddToWaitingList(name: String) extends ServerProtocol[Unit]
  final case class RemoveFromMatchmaking(name: String)
      extends ServerProtocol[Unit]
  final case class GetWaitingList() extends ServerProtocol[Set[String]]
  final case class RegisterMatch(playerA: String, playerB: String)
      extends ServerProtocol[Unit]
  final case class GetOpponentName(player: String)
      extends ServerProtocol[Option[String]]
}
