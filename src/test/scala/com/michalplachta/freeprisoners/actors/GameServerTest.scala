package com.michalplachta.freeprisoners.actors

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.TestKit
import com.michalplachta.freeprisoners.PrisonersDilemma.{Guilty, Prisoner}
import com.michalplachta.freeprisoners.actors.GameServer.{
  CreateNewGame,
  GetSavedDecision,
  SaveDecision
}
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import org.scalatest.{AsyncWordSpecLike, Matchers}

import scala.concurrent.duration._

class GameServerTest
    extends TestKit(ActorSystem("gameServerTest"))
    with AsyncWordSpecLike
    with Matchers {
  "GameServer actor" should {
    "return the same gameId for player and opponent" in {
      val player = Prisoner("Player")
      val opponent = Prisoner("Opponent")
      val server = createServer()
      for {
        playerGameId <- askServer(server,
                                  CreateNewGame(player, opponent),
                                  maxRetries = 1,
                                  retryTimeout = 1.second)
        opponentGameId <- askServer(server,
                                    CreateNewGame(opponent, player),
                                    maxRetries = 1,
                                    retryTimeout = 1.second)
      } yield playerGameId should equal(opponentGameId)
    }

    "return different gameIds for the same match is created twice" in {
      val player = Prisoner("Player")
      val opponent = Prisoner("Opponent")
      val server = createServer()
      for {
        firstGameId <- askServer(server,
                                 CreateNewGame(player, opponent),
                                 maxRetries = 1,
                                 retryTimeout = 1.second)
        secondGameId <- askServer(server,
                                  CreateNewGame(player, opponent),
                                  maxRetries = 1,
                                  retryTimeout = 1.second)
      } yield firstGameId shouldNot equal(secondGameId)
    }

    "save the decision of the player" in {
      val player = Prisoner("Player")
      val opponent = Prisoner("Opponent")
      val server = createServer()
      for {
        gameId <- askServer(server,
                            CreateNewGame(player, opponent),
                            maxRetries = 1,
                            retryTimeout = 1.second)
        _ <- tellServer(server, SaveDecision(gameId, player, Guilty))
        decision <- askServer(server,
                              GetSavedDecision(gameId, player),
                              maxRetries = 1,
                              retryTimeout = 1.second)
      } yield decision should contain(Guilty)
    }

    "save the decision of the player just for one game" in {
      val player = Prisoner("Player")
      val opponent = Prisoner("Opponent")
      val server = createServer()

      for {
        gameId <- askServer(server,
                            CreateNewGame(player, opponent),
                            maxRetries = 1,
                            retryTimeout = 1.second)
        _ <- tellServer(server, SaveDecision(gameId, player, Guilty))
        differentGameId <- askServer(server,
                                     CreateNewGame(player, opponent),
                                     maxRetries = 1,
                                     retryTimeout = 1.second)
        decision <- askServer(server,
                              GetSavedDecision(differentGameId, player),
                              maxRetries = 1,
                              retryTimeout = 1.second)
      } yield decision should be(None)
    }
  }

  private def createServer() =
    ActorSelection(system.actorOf(Props[GameServer]), "/")
}
