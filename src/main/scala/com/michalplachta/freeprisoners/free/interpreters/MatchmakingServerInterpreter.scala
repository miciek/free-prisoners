package com.michalplachta.freeprisoners.free.interpreters

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.effect.IO
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.actors.MatchmakingServer._
import com.michalplachta.freeprisoners.actors.ServerCommunication._
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking.WaitingPlayer
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps._
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

class MatchmakingServerInterpreter extends (Matchmaking ~> IO) {
  private val system = ActorSystem("matchmakingClient")
  private val config = ConfigFactory.load().getConfig("app.matchmaking")
  private val maxRetries = config.getInt("client.max-retries")
  private val retryTimeout = Timeout(
    config.getDuration("client.retry-timeout").toMillis,
    TimeUnit.MILLISECONDS)
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val server =
    system.actorSelection(config.getString("server.path"))

  /*_*/
  def apply[A](matchmaking: Matchmaking[A]): IO[A] = matchmaking match {
    case RegisterAsWaiting(player) =>
      tellServer(server, AddToWaitingList(player.name))
    case UnregisterWaiting(player) =>
      tellServer(server, RemoveFromWaitingList(player.name))
    case GetWaitingPlayers() =>
      askServer(server, GetWaitingList(), maxRetries, retryTimeout)
        .map(_.map(name => WaitingPlayer(Prisoner(name))))
    case JoinWaitingPlayer(player, waitingPlayer) =>
      for {
        _ <- tellServer(server,
                        RegisterMatch(player.name, waitingPlayer.prisoner.name))
        opponentName <- askServer(server,
                                  GetOpponentNameFor(player.name),
                                  maxRetries,
                                  retryTimeout)
      } yield opponentName.map(Prisoner)
    case CheckIfOpponentJoined(player) =>
      askServer(server,
                GetOpponentNameFor(player.name),
                maxRetries,
                retryTimeout)
        .map(_.map(Prisoner))
  }

  def terminate(): Unit = system.terminate()
}
