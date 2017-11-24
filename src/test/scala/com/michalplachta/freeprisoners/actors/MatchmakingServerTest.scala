package com.michalplachta.freeprisoners.actors

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.TestKit
import com.michalplachta.freeprisoners.actors.MatchmakingServer._
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.duration._

class MatchmakingServerTest
    extends TestKit(ActorSystem("matchmakingServerTest"))
    with AsyncWordSpecLike
    with Matchers {
  "MatchmakingServer actor" should {
    "add player names to the waiting list" in {
      val server = createServer()
      tellServer(server, AddToWaitingList("a"))
      tellServer(server, AddToWaitingList("b"))
      askServer(server, GetWaitingList(), 1, 1.second)
        .map(_ should be(Set("a", "b")))
    }

    "remove player name from the waiting list when it's removed from matchmaking" in {
      val server = createServer()
      tellServer(server, AddToWaitingList("a"))
      tellServer(server, AddToWaitingList("b"))
      tellServer(server, RemoveFromWaitingList("a"))
      askServer(server, GetWaitingList(), 1, 1.second)
        .map(_ should be(Set("b")))
    }

    "respond with the opponent after match is registered" in {
      val server = createServer()
      tellServer(server, RegisterMatch("a", "b"))
      askServer(server, GetOpponentNameFor("a"), 1, 1.second)
        .map(_ should contain("b"))

      askServer(server, GetOpponentNameFor("b"), 1, 1.second)
        .map(_ should contain("a"))
    }

    "respond with no opponent when the player is only on the waiting list" in {
      val server = createServer()
      tellServer(server, AddToWaitingList("a"))
      askServer(server, GetOpponentNameFor("a"), 1, 1.second)
        .map(_ should be(None))
    }

    "remove player names from the registered matches when one of them is added to waiting list" in {
      val server = createServer()
      tellServer(server, RegisterMatch("a", "b"))
      tellServer(server, AddToWaitingList("a"))

      askServer(server, GetOpponentNameFor("a"), 1, 1.second)
        .map(_ should be(None))

      askServer(server, GetOpponentNameFor("b"), 1, 1.second)
        .map(_ should be(None))
    }
  }

  private def createServer() =
    ActorSelection(system.actorOf(Props[MatchmakingServer]), "/")
}
