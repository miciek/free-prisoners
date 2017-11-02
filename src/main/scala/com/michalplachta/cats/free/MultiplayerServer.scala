package com.michalplachta.cats.free

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.michalplachta.cats.free.PrisonersDilemma._
import com.typesafe.config.ConfigFactory

object MultiplayerServer extends App {
  final case class PrisonerPlayer(name: String,
                                  actorRef: ActorRef,
                                  decision: Option[Decision])

  class Server extends Actor with ActorLogging {
    var playerA: Option[PrisonerPlayer] = None
    var playerB: Option[PrisonerPlayer] = None

    def receive: Receive = {
      case playerName: String =>
        endGameIfAllPlayersDefined()
        val newPlayer = PrisonerPlayer(playerName, sender, None)
        if (playerA.isEmpty) {
          playerA = Some(newPlayer)
        } else {
          playerB = Some(newPlayer)
          announcePlayers()
        }
        log.info(s"Registered $playerName")

      case (playerName: String, opponentName: String, decisionStr: String) =>
        def playerWithDecision(player: Option[PrisonerPlayer],
                               opponent: Option[PrisonerPlayer],
                               playerDecision: Decision) =
          if (player.exists(_.name == playerName))
            player.map(_.copy(decision = Some(playerDecision)))
          else player

        val playerDecision = if (decisionStr == "Guilty") Guilty else Silence
        playerA = playerWithDecision(playerA, playerB, playerDecision)
        playerB = playerWithDecision(playerB, playerA, playerDecision)

        endGameIfAllDecisionsReceived()
        log.info(
          s"Received decision of $playerName against $opponentName: $decisionStr ($playerDecision)")
    }

    private def announcePlayers(): Unit = {
      def announcePlayer(maybePlayer: Option[PrisonerPlayer],
                         maybeOpponent: Option[PrisonerPlayer]) =
        for {
          player <- maybePlayer
          opponent <- maybeOpponent
        } yield player.actorRef ! opponent.name
      announcePlayer(playerA, playerB)
      announcePlayer(playerB, playerA)
    }

    private def endGameIfAllDecisionsReceived(): Unit =
      if (playerA.exists(_.decision.isDefined)
          && playerB.exists(_.decision.isDefined)) endGame()

    private def endGameIfAllPlayersDefined(): Unit =
      if (playerA.isDefined && playerB.isDefined) endGame()

    private def endGame(): Unit = {
      resolveGame()
      playerA = None
      playerB = None
    }

    private def resolveGame(): Unit = {
      def decision(player: Option[PrisonerPlayer]) =
        player.flatMap(_.decision).getOrElse(Silence)
      def sendVerdict(player: Option[PrisonerPlayer],
                      opponent: Option[PrisonerPlayer]): Unit =
        player.foreach {
          _.actorRef ! verdict(decision(player), decision(opponent)).years
        }
      sendVerdict(playerA, playerB)
      sendVerdict(playerB, playerA)
      log.info(s"Resolved game between $playerA and $playerB")
    }
  }

  val system = ActorSystem("prisonersDilemma", ConfigFactory.load("server"))
  val server = system.actorOf(Props[Server], "server")
}
