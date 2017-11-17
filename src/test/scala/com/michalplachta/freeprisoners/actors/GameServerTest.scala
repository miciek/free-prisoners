package com.michalplachta.freeprisoners.actors

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.TestKit
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence
}
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.actors.GameServer.{
  ClearSavedDecisions,
  GetSavedDecision,
  SaveDecision
}
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.duration._

class GameServerTest
    extends TestKit(ActorSystem("gameServerTest"))
    with AsyncWordSpecLike
    with Matchers {
  "GameServer actor" should {
    "save the decision of the player" in {
      val player = Prisoner("Player")
      val opponent = Prisoner("Opponent")
      val server = createServer()

      tellServer(server, SaveDecision(player, opponent, Guilty))
      askServer(server, GetSavedDecision(player, opponent), 1, 1.second)
        .map(_ should contain(Guilty))
    }

    "clear the decisions of the player" in {
      val player = Prisoner("Player")
      val opponentA = Prisoner("OpponentA")
      val opponentB = Prisoner("OpponentB")
      val server = createServer()

      tellServer(server, SaveDecision(player, opponentA, Guilty))
      tellServer(server, SaveDecision(player, opponentB, Silence))
      tellServer(server, ClearSavedDecisions(player))
      askServer(server, GetSavedDecision(player, opponentA), 1, 1.second)
        .map(_ should be(None))
      askServer(server, GetSavedDecision(player, opponentB), 1, 1.second)
        .map(_ should be(None))
    }
  }

  private def createServer() =
    ActorSelection(system.actorOf(Props[GameServer]), "/")
}
