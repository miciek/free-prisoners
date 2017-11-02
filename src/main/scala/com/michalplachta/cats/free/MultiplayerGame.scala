package com.michalplachta.cats.free

import cats.implicits.catsStdInstancesForFuture
import com.michalplachta.cats.free.LocalMultiplayerGame.Multiplayer
import com.michalplachta.cats.free.PlayerDSL.Player
import com.michalplachta.cats.free.ServerDSL.Server

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object MultiplayerGame extends App {
  try {
    val gameResult = LocalMultiplayerGame
      .program(
        new Player.Ops[Multiplayer],
        new Server.Ops[Multiplayer]
      )
      .foldMap(
        PlayerConsoleInterpreter.andThen(IdToFuture) or RemoteServerInterpreter)
    Await.result(gameResult, 60.seconds)
  } finally {
    RemoteServerInterpreter.terminate()
  }
}
