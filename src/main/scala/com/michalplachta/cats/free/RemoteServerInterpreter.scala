package com.michalplachta.cats.free

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import cats.~>
import com.michalplachta.cats.free.PrisonersDilemma.{Prisoner, Verdict}
import com.michalplachta.cats.free.ServerDSL.{
  GetOpponentFor,
  SendDecision,
  Server
}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object RemoteServerInterpreter extends (Server ~> Future) {
  val system = ActorSystem("gameClient", ConfigFactory.load("client"))
  val server: ActorSelection = system.actorSelection(
    "akka.tcp://prisonersDilemma@127.0.0.1:2552/user/server")

  /*
   * Note: the protocol between client and remote server uses Strings only.
   */
  def apply[A](i: Server[A]): Future[A] = i match {
    case GetOpponentFor(prisoner) =>
      server
        .ask(prisoner.name)(60.seconds)
        .mapTo[String]
        .map(Prisoner)

    case SendDecision(prisoner, otherPrisoner, decision) =>
      // FIXME: this doesn't work properly with the current MultiplayerServer implementation
      server
        .ask((prisoner.name, otherPrisoner.name, decision.toString))(60.seconds)
        .mapTo[Int]
        .map(Verdict)
  }

  def terminate(): Unit = system.terminate()
}
