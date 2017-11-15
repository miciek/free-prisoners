package com.michalplachta.freeprisoners.interpreters

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import cats.~>
import com.michalplachta.freeprisoners.apps.MatchmakingServer._
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking.WaitingPlayer
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

class MatchmakingServerInterpreter extends (Matchmaking ~> Future) {
  private val system = ActorSystem("matchmakingClient")
  private val config = ConfigFactory.load
  private val maxRetries = config.getInt("app.matchmaking-client.max-retries")
  private val retryTimeout = Timeout(
    config.getDuration("app.matchmaking-client.retry-timeout").toMillis,
    TimeUnit.MILLISECONDS)

  private val server =
    system.actorSelection(config.getString("app.game-server-path"))

  private def askServer[T: ClassTag](message: ServerProtocol[T],
                                     retries: Int = maxRetries): Future[T] = {
    println(s"Asking server: $message, retries left: $retries")
    val futurePrisoner =
      server.ask(message)(retryTimeout).mapTo[T]
    if (retries > maxRetries) {
      futurePrisoner.recoverWith({
        case _ => askServer(message, retries - 1)
      })
    } else futurePrisoner
  }

  private def tellServer(message: ServerProtocol[Unit]): Future[Unit] = {
    server ! message
    Future.successful(())
  }

  def apply[A](matchmaking: Matchmaking[A]): Future[A] = matchmaking match {
    case RegisterAsWaiting(player) =>
      tellServer(AddToWaitingList(player.name))
    case UnregisterPlayer(player) =>
      tellServer(RemoveFromWaitingList(player.name))
    case GetWaitingPlayers() =>
      askServer(GetWaitingList()).map(_.map(name =>
        WaitingPlayer(Prisoner(name))))
    case JoinWaitingPlayer(player, waitingPlayer) =>
      tellServer(RegisterGame(player.name, waitingPlayer.prisoner.name))
      askServer(GetOpponentName(player.name)).map(_.map(Prisoner))
    case WaitForOpponentToJoin(player, maxWaitTime) =>
      askServer(
        GetOpponentName(player.name),
        (maxWaitTime / retryTimeout.duration).toInt).map(_.map(Prisoner))
  }

  def terminate(): Unit = system.terminate()
}
