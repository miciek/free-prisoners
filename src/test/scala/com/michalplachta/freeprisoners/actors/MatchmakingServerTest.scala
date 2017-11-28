package com.michalplachta.freeprisoners.actors

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.TestKit
import com.michalplachta.freeprisoners.actors.MatchmakingServer._
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import org.scalatest.{AsyncWordSpecLike, BeforeAndAfterAll, Matchers}

class MatchmakingServerTest
    extends TestKit(ActorSystem("matchmakingServerTest"))
    with AsyncWordSpecLike
    with Matchers
    with BeforeAndAfterAll {
  "MatchmakingServer actor" should {
    "add player names to the waiting list" in {
      val server = createServer()
      val program = for {
        _ <- tellServer(server, AddToWaitingList("a"))
        _ <- tellServer(server, AddToWaitingList("b"))
        result <- askServer(server, GetWaitingList())
      } yield result.toSet should be(Set("a", "b"))
      program.unsafeToFuture()
    }

    "remove the player name from the waiting list" in {
      val server = createServer()
      val program = for {
        _ <- tellServer(server, AddToWaitingList("a"))
        _ <- tellServer(server, AddToWaitingList("b"))
        _ <- tellServer(server, RemoveFromWaitingList("a"))
        result <- askServer(server, GetWaitingList())
      } yield result.toSet should be(Set("b"))
      program.unsafeToFuture()
    }

    "respond with the opponent name after match is registered" in {
      val server = createServer()
      val program = for {
        _ <- tellServer(server, RegisterMatch("a", "b"))
        opponentOfA <- askServer(server, GetOpponentNameFor("a"))
        opponentOfB <- askServer(server, GetOpponentNameFor("b"))
      } yield (opponentOfA, opponentOfB) should be((Some("b"), Some("a")))
      program.unsafeToFuture()
    }

    "respond with no opponent name when only one player is on the waiting list" in {
      val server = createServer()
      val program = for {
        _ <- tellServer(server, AddToWaitingList("a"))
        opponent <- askServer(server, GetOpponentNameFor("a"))
      } yield opponent should be(None)
      program.unsafeToFuture()
    }

    "remove player names from the registered matches when one of them is added back to the waiting list" in {
      val server = createServer()
      val program = for {
        _ <- tellServer(server, RegisterMatch("a", "b"))
        _ <- tellServer(server, AddToWaitingList("a"))
        opponentOfA <- askServer(server, GetOpponentNameFor("a"))
        opponentOfB <- askServer(server, GetOpponentNameFor("b"))
      } yield (opponentOfA, opponentOfB) should be((None, None))
      program.unsafeToFuture()
    }
  }

  override def afterAll(): Unit = system.terminate()

  private def createServer() =
    ActorSelection(system.actorOf(Props[MatchmakingServer]), "/")
}
