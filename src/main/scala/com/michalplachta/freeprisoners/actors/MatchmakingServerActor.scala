package com.michalplachta.freeprisoners.actors

import akka.actor.{Actor, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import com.michalplachta.freeprisoners.actors.MatchmakingServerActor._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class MatchmakingServerActor extends Actor {
  private var waitingList = Set.empty[String]
  private var matches = Set.empty[Set[String]]

  def receive: Receive = {
    case AddToWaitingList(name) =>
      waitingList = waitingList + name

    case RemoveFromMatchmaking(name) =>
      waitingList = waitingList - name
      matches = matches.filterNot(_.contains(name))

    case GetWaitingList() =>
      sender ! waitingList

    case RegisterMatch(playerA, playerB) =>
      matches += Set(playerA, playerB)

    case GetOpponentName(player) =>
      val game: Set[String] = matches.find(_.contains(player)).toSet.flatten
      sender ! game.filterNot(_ == player).headOption
  }
}

object MatchmakingServerActor {
  sealed trait ServerProtocol[A]
  final case class AddToWaitingList(name: String) extends ServerProtocol[Unit]
  final case class RemoveFromMatchmaking(name: String)
      extends ServerProtocol[Unit]
  final case class GetWaitingList() extends ServerProtocol[Set[String]]
  final case class RegisterMatch(playerA: String, playerB: String)
      extends ServerProtocol[Unit]
  final case class GetOpponentName(player: String)
      extends ServerProtocol[Option[String]]

  def askServer[T: ClassTag](
      server: ActorSelection,
      message: ServerProtocol[T],
      maxRetries: Int,
      retryTimeout: Timeout)(implicit ec: ExecutionContext): Future[T] = {
    def loop(retries: Int = maxRetries): Future[T] = {
      println(s"Asking server: $message, retries left: $retries")
      val response = server.ask(message)(retryTimeout).mapTo[T]
      if (retries > 0) {
        response.recoverWith({ case _ => loop(retries - 1) })
      } else response
    }
    loop(maxRetries)
  }

  def tellServer(server: ActorSelection, message: ServerProtocol[Unit])(
      implicit ec: ExecutionContext): Future[Unit] = {
    server ! message
    Future.successful(())
  }
}
