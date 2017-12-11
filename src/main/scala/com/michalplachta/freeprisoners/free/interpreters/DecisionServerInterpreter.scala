package com.michalplachta.freeprisoners.free.interpreters

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.effect.IO
import cats.~>
import com.michalplachta.freeprisoners.actors.DecisionServer.{
  ClearSavedDecision,
  GetSavedDecision,
  SaveDecision
}
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.free.algebras.DecisionRegistryOps._
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

class DecisionServerInterpreter extends (DecisionRegistry ~> IO) {
  private val system = ActorSystem("decisionClient")
  private val config = ConfigFactory.load().getConfig("app.decision")
  private val maxRetries = config.getInt("client.max-retries")
  private val retryTimeout = Timeout(
    config.getDuration("client.retry-timeout").toMillis,
    TimeUnit.MILLISECONDS)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val server =
    system.actorSelection(config.getString("server.path"))

  /*_*/
  def apply[A](decisionRegistry: DecisionRegistry[A]): IO[A] =
    decisionRegistry match {
      case RegisterDecision(prisoner, decision) =>
        tellServer(server, SaveDecision(prisoner, decision))
      case GetRegisteredDecision(prisoner) =>
        askServer(server, GetSavedDecision(prisoner), maxRetries, retryTimeout)
      case ClearRegisteredDecision(prisoner) =>
        tellServer(server, ClearSavedDecision(prisoner))
    }

  def terminate(): Unit = system.terminate()
}
