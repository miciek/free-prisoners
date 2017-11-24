package com.michalplachta.freeprisoners.actors

import akka.actor.Actor
import com.michalplachta.freeprisoners.actors.MatchmakingServer._

class MatchmakingServer extends Actor {
  private var waitingList = Set.empty[String]
  private var matches = Set.empty[Set[String]]

  def receive: Receive = {
    case AddToWaitingList(name) =>
      matches = matches.filterNot(_.contains(name))
      waitingList = waitingList + name

    case RemoveFromWaitingList(name) =>
      waitingList = waitingList - name

    case GetWaitingList() =>
      sender ! waitingList

    case RegisterMatch(playerA, playerB) =>
      matches += Set(playerA, playerB)

    case GetOpponentNameFor(player) =>
      val game: Set[String] = matches.find(_.contains(player)).toSet.flatten
      sender ! game.filterNot(_ == player).headOption
  }
}

object MatchmakingServer {
  sealed trait ServerProtocol[A]
  final case class AddToWaitingList(name: String) extends ServerProtocol[Unit]
  final case class RemoveFromWaitingList(name: String)
      extends ServerProtocol[Unit]
  final case class GetWaitingList() extends ServerProtocol[Set[String]]
  final case class RegisterMatch(playerA: String, playerB: String)
      extends ServerProtocol[Unit]
  final case class GetOpponentNameFor(player: String)
      extends ServerProtocol[Option[String]]
}
