package com.michalplachta.freeprisoners.freestyle.handlers

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.actors.MatchmakingServer._
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.freestyle.algebras.{
  Matchmaking,
  WaitingPlayer
}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

class MatchmakingServerHandler extends Matchmaking.Handler[IO] {
  private val system = ActorSystem("matchmakingClient")
  private val config = ConfigFactory.load().getConfig("app.matchmaking")
  private val maxRetries = config.getInt("client.max-retries")
  private val retryTimeout = Timeout(
    config.getDuration("client.retry-timeout").toMillis,
    TimeUnit.MILLISECONDS)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val server =
    system.actorSelection(config.getString("server.path"))

  override def registerAsWaiting(player: Prisoner) = {
    tellServer(server, AddToWaitingList(player.name))
  }

  override def unregisterPlayer(player: Prisoner) = {
    tellServer(server, RemoveFromWaitingList(player.name))
  }

  override def getWaitingPlayers = {
    askServer(server, GetWaitingList(), maxRetries, retryTimeout)
      .map(_.map(name => WaitingPlayer(Prisoner(name))))
  }

  override def joinWaitingPlayer(player: Prisoner,
                                 waitingPlayer: WaitingPlayer) = {
    for {
      _ <- tellServer(server,
                      RegisterMatch(player.name, waitingPlayer.prisoner.name))
      opponentName <- askServer(server,
                                GetOpponentNameFor(player.name),
                                maxRetries,
                                retryTimeout)
    } yield opponentName.map(Prisoner)
  }

  override def checkIfOpponentJoined(player: Prisoner) = {
    askServer(server, GetOpponentNameFor(player.name), maxRetries, retryTimeout)
      .map(_.map(Prisoner))
  }

  def terminate(): Unit = system.terminate()
}
