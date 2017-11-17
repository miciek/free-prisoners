package com.michalplachta.freeprisoners.actors

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.TestKit
import com.michalplachta.freeprisoners.PrisonersDilemma.{Guilty, Prisoner}
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.actors.GameServer.{
  GetSavedDecision,
  SaveDecision
}
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.duration._

class GameServerTest
    extends TestKit(ActorSystem("gameServerTest"))
    with AsyncWordSpecLike
    with Matchers {
  "Game server actor" should {
    "save the decision of the player" in {
      val player = Prisoner("Player")
      val opponent = Prisoner("Opponent")
      val server = createServer()

      tellServer(server, SaveDecision(player, opponent, Guilty))
      askServer(server, GetSavedDecision(player, opponent), 1, 1.second)
        .map(_ should contain(Guilty))
    }
  }

  private def createServer() =
    ActorSelection(system.actorOf(Props[GameServer]), "/")
}
