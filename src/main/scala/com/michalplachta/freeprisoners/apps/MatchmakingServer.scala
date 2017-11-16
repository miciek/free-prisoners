package com.michalplachta.freeprisoners.apps

import akka.actor.{ActorSystem, Props}
import com.michalplachta.freeprisoners.actors.MatchmakingServerActor

object MatchmakingServer extends App {
  private val system = ActorSystem("prisonersDilemma")
  system.actorOf(Props[MatchmakingServerActor], "server")
  println("Server is running...")
}
