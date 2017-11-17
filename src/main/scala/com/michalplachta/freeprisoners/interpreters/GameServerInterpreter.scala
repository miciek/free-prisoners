package com.michalplachta.freeprisoners.interpreters

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.~>
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.actors.GameServer.{
  GetSavedDecision,
  SaveDecision
}
import com.michalplachta.freeprisoners.algebras.GameOps.{
  Game,
  GetOpponentDecision,
  SendDecision
}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

class GameServerInterpreter extends (Game ~> Future) {
  private val system = ActorSystem("gameClient")
  private val config = ConfigFactory.load().atPath("app.game")
  private val maxRetries = config.getInt("client.max-retries")
  private val retryTimeout = Timeout(
    config.getDuration("client.retry-timeout").toMillis,
    TimeUnit.MILLISECONDS)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val server =
    system.actorSelection(config.getString("server.path"))

  def apply[A](game: Game[A]): Future[A] = game match {
    case SendDecision(player, opponent, decision) =>
      tellServer(server, SaveDecision(player, opponent, decision))
    case GetOpponentDecision(player, opponent, maxWaitTime) =>
      askServer(server,
                GetSavedDecision(opponent, player),
                (maxWaitTime / retryTimeout.duration).toInt,
                retryTimeout)
  }

  def terminate(): Unit = system.terminate()
}
