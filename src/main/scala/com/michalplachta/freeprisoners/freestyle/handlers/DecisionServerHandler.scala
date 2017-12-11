package com.michalplachta.freeprisoners.freestyle.handlers

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.actors.DecisionServer.{
  GetSavedDecision,
  SaveDecision
}
import com.michalplachta.freeprisoners.freestyle.algebras.DecisionRegistry
import com.typesafe.config.ConfigFactory
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.free.algebras.DecisionRegistryOps.ClearRegisteredDecision

import scala.concurrent.ExecutionContext

class DecisionServerHandler extends DecisionRegistry.Handler[IO] {
  private val system = ActorSystem("decisionClient")
  private val config = ConfigFactory.load().getConfig("app.decision")
  private val maxRetries = config.getInt("client.max-retries")
  private val retryTimeout = Timeout(
    config.getDuration("client.retry-timeout").toMillis,
    TimeUnit.MILLISECONDS)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val server =
    system.actorSelection(config.getString("server.path"))

  override def registerDecision(prisoner: Prisoner, decision: Decision) = {
    tellServer(server, SaveDecision(prisoner, decision))
  }

  override def getRegisteredDecision(prisoner: Prisoner) = {
    askServer(server, GetSavedDecision(prisoner), maxRetries, retryTimeout)
  }

  override def clearRegisteredDecision(prisoner: Prisoner) = {
    tellServer(server, ClearRegisteredDecision(prisoner))
  }

  def terminate(): Unit = system.terminate()
}
