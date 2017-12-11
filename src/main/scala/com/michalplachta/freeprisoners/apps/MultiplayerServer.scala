package com.michalplachta.freeprisoners.apps

import akka.actor.{ActorSystem, Props}
import com.michalplachta.freeprisoners.actors.{
  DecisionServer,
  MatchmakingServer
}

object MultiplayerServer extends App {
  private val system = ActorSystem("prisonersDilemma")
  system.actorOf(Props[MatchmakingServer], "matchmakingServer")
  system.actorOf(Props[DecisionServer], "decisionServer")
  println("Server is running...")
}
