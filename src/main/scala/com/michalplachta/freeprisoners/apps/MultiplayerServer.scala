package com.michalplachta.freeprisoners.apps

import akka.actor.{ActorSystem, Props}
import com.michalplachta.freeprisoners.actors.{
  GameServerActor,
  MatchmakingServerActor
}

object MultiplayerServer extends App {
  private val system = ActorSystem("prisonersDilemma")
  system.actorOf(Props[MatchmakingServerActor], "matchmakingServer")
  system.actorOf(Props[GameServerActor], "gameServer")
  println("Server is running...")
}
