package com.michalplachta.freeprisoners

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import cats.~>
import com.michalplachta.freeprisoners.BotDSL.Strategies
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.ServerDSL.{
  GetDecision,
  GetOpponentFor,
  SendDecision,
  Server
}

import scala.concurrent.Future
import scala.concurrent.duration._

object LocalServerInterpreter extends (Server ~> Future) {
  private final case class SendPlayerDecision(decision: Decision)
  private final case class GetPrisonerDecision(prisoner: Prisoner)

  class ServerActor extends Actor with ActorLogging {
    val bot = Prisoner("Gulliver")
    var playerDecision: Option[Decision] = None

    def receive: Receive = {
      case prisoner: Prisoner =>
        log.info(s"Registering new prisoner $prisoner. Opponent: $bot")
        playerDecision = None
        sender ! bot

      case SendPlayerDecision(decision) =>
        playerDecision = Some(decision)

      case GetPrisonerDecision(prisoner) =>
        if (prisoner == bot) {
          val botDecision = Strategies.alwaysBlame(prisoner)
          log.info(s"Bot $bot decision: $botDecision")
          sender ! botDecision
        }
    }
  }

  val system = ActorSystem("ServerSystem")
  val server: ActorRef = system.actorOf(Props[ServerActor])

  def apply[A](i: Server[A]): Future[A] = i match {
    case GetOpponentFor(prisoner) =>
      server.ask(prisoner)(5.seconds).mapTo[Prisoner]

    case SendDecision(prisoner, otherPrisoner, decision) =>
      server ! SendPlayerDecision(decision)
      Future.successful(())

    case GetDecision(prisoner) =>
      server
        .ask(GetPrisonerDecision(prisoner))(5.seconds)
        .mapTo[Decision]
  }

  def terminate(): Unit = system.terminate()
}
