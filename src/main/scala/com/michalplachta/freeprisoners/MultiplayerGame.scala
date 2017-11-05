package com.michalplachta.freeprisoners

import cats.implicits.catsStdInstancesForFuture
import com.michalplachta.freeprisoners.LocalMultiplayerGame.Multiplayer
import com.michalplachta.freeprisoners.algebras.PlayerDSL.Player
import com.michalplachta.freeprisoners.algebras.ServerDSL.Server

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
