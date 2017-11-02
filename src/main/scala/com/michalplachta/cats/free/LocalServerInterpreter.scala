package com.michalplachta.cats.free

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import cats.~>
import com.michalplachta.cats.free.BotDSL.Strategies
import com.michalplachta.cats.free.PrisonersDilemma.{
  Decision,
  OtherPrisoner,
  Prisoner,
  Verdict
}
import com.michalplachta.cats.free.ServerDSL.{
  GetOpponentFor,
  SendDecision,
  Server
}

import scala.concurrent.duration._
import scala.concurrent.Future

object LocalServerInterpreter extends (Server ~> Future) {
  final case class GetVerdict(prisoner: Prisoner,
                              otherPrisoner: OtherPrisoner,
                              decision: Decision)
  class ServerActor extends Actor with ActorLogging {
    val bot = Prisoner("Gulliver")

    def receive: Receive = {
      case prisoner: Prisoner =>
        log.info(s"Registering new prisoner $prisoner. Opponent: $bot")
        sender ! bot

      case GetVerdict(prisoner, _, decision) =>
        val botDecision = Strategies.alwaysBlame(prisoner)
        log.info(s"Bot $bot decision: $botDecision")
        sender ! PrisonersDilemma.verdict(decision, botDecision)
    }
  }

  val system = ActorSystem("ServerSystem")
  val server: ActorRef = system.actorOf(Props[ServerActor])

  def apply[A](i: Server[A]): Future[A] = i match {
    case GetOpponentFor(prisoner) =>
      server.ask(prisoner)(5.seconds).mapTo[Prisoner]
    case SendDecision(prisoner, otherPrisoner, decision) =>
      server
        .ask(GetVerdict(prisoner, otherPrisoner, decision))(5.seconds)
        .mapTo[Verdict]
  }

  def terminate(): Unit = system.terminate()
}
