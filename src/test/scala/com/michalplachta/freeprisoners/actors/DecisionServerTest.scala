package com.michalplachta.freeprisoners.actors

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.TestKit
import com.michalplachta.freeprisoners.PrisonersDilemma.{Guilty, Prisoner}
import com.michalplachta.freeprisoners.actors.DecisionServer.{
  ClearSavedDecision,
  GetSavedDecision,
  SaveDecision
}
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import org.scalatest.{AsyncWordSpecLike, BeforeAndAfterAll, Matchers}

class DecisionServerTest
    extends TestKit(ActorSystem("decisionServerTest"))
    with AsyncWordSpecLike
    with Matchers
    with BeforeAndAfterAll {
  "DecisionServer actor" should {
    "save the decision of the player" in {
      val player = Prisoner("Player")
      val server = createServer()
      val program = for {
        _ <- tellServer(server, SaveDecision(player, Guilty))
        decision <- askServer(server, GetSavedDecision(player))
      } yield decision should contain(Guilty)
      program.unsafeToFuture()
    }

    "allow to clear the decision between the games" in {
      val player = Prisoner("Player")
      val server = createServer()
      val program = for {
        _ <- tellServer(server, SaveDecision(player, Guilty))
        _ <- tellServer(server, ClearSavedDecision(player))
        decision <- askServer(server, GetSavedDecision(player))
      } yield decision should be(None)
      program.unsafeToFuture()
    }
  }

  override def afterAll(): Unit = system.terminate()

  private def createServer() =
    ActorSelection(system.actorOf(Props[DecisionServer]), "/")
}
