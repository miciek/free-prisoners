package com.michalplachta.freeprisoners.apps

import akka.actor.{Actor, ActorSystem, Props}

// TODO: this will replace previous implementation in {{freeprisoners}} package
object MatchmakingServer extends App {
  sealed trait ServerProtocol[A]
  final case class AddToWaitingList(name: String) extends ServerProtocol[Unit]
  final case class RemoveFromWaitingList(name: String)
      extends ServerProtocol[Unit]
  final case class GetWaitingList() extends ServerProtocol[Set[String]]
  final case class RegisterGame(playerA: String, playerB: String)
      extends ServerProtocol[Unit]
  final case class GetOpponentName(player: String)
      extends ServerProtocol[Option[String]]

  private class Server extends Actor {
    private var waitingList = Set.empty[String]
    private var games = Set.empty[Set[String]]

    def receive: Receive = {
      case AddToWaitingList(name) =>
        waitingList = waitingList + name

      case RemoveFromWaitingList(name) =>
        waitingList = waitingList - name

      case GetWaitingList() =>
        sender ! waitingList

      case RegisterGame(playerA, playerB) =>
        games += Set(playerA, playerB)

      case GetOpponentName(player) =>
        val game: Set[String] = games.find(_.contains(player)).toSet.flatten
      // TODO
    }
  }

  private val system = ActorSystem("gameServer")
  system.actorOf(Props[Server], "server")
  println("Server is running...")
}
