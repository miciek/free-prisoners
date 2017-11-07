package com.michalplachta.freeprisoners.interpreters

import akka.actor.ActorSystem
import akka.pattern.ask
import cats.~>
import com.michalplachta.freeprisoners.MultiplayerServer._
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.ServerOps.{
  GetDecision,
  GetOpponentFor,
  SendDecision,
  Server
}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

class RemoteServerInterpreter extends (Server ~> Future) {
  private val system = ActorSystem("gameClient")
  private val server =
    system.actorSelection(ConfigFactory.load.getString("app.game-server-path"))

  private def askServer[T: ClassTag](message: ServerProtocol[T],
                                     retries: Int = 0): Future[T] = {
    println(s"Asking server: $message, retry $retries")
    val futurePrisoner =
      server.ask(message)(2.seconds).mapTo[T]
    if (retries < 20) {
      futurePrisoner.recoverWith({
        case _ => askServer(message, retries + 1)
      })
    } else futurePrisoner
  }

  def apply[A](i: Server[A]): Future[A] = i match {
    case GetOpponentFor(prisoner) =>
      server ! RegisterPlayer(prisoner.name)
      askServer(GetPlayerOpponent(prisoner.name)).map(Prisoner)

    case SendDecision(prisoner, otherPrisoner, decision) =>
      server ! RegisterDecision(prisoner.name, decision)
      Future.successful(())

    case GetDecision(prisoner) =>
      askServer(GetRegisteredDecision(prisoner.name))
  }

  def terminate(): Unit = system.terminate()
}
