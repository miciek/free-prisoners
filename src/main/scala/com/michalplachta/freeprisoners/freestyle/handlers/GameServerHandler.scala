package com.michalplachta.freeprisoners.freestyle.handlers

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.actors.GameServer.{
  GetGameId,
  GetSavedDecision,
  SaveDecision
}
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.freestyle.algebras.Game
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

class GameServerHandler extends Game.Handler[IO] {
  private val system = ActorSystem("gameClient")
  private val config = ConfigFactory.load().getConfig("app.game")
  private val maxRetries = config.getInt("client.max-retries")
  private val retryTimeout = Timeout(
    config.getDuration("client.retry-timeout").toMillis,
    TimeUnit.MILLISECONDS)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val server =
    system.actorSelection(config.getString("server.path"))

  override def getGameHandle(player: PrisonersDilemma.Prisoner,
                             opponent: PrisonersDilemma.Prisoner) = {
    askServer(server, GetGameId(player, opponent), maxRetries, retryTimeout)
  }

  override def sendDecision(gameHandle: UUID,
                            player: PrisonersDilemma.Prisoner,
                            decision: PrisonersDilemma.Decision) = {
    tellServer(server, SaveDecision(gameHandle, player, decision))
  }

  override def getOpponentDecision(gameHandle: UUID,
                                   opponent: PrisonersDilemma.Prisoner) = {
    askServer(server,
              GetSavedDecision(gameHandle, opponent),
              maxRetries,
              retryTimeout)
  }

  def terminate(): Unit = system.terminate()
}
