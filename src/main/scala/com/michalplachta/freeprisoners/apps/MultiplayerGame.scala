package com.michalplachta.freeprisoners.apps

import cats.implicits.catsStdInstancesForFuture
import cats.~>
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.interpreters._
import com.michalplachta.freeprisoners.programs.Multiplayer
import com.michalplachta.freeprisoners.programs.Multiplayer.{
  Multiplayer,
  Multiplayer0,
  Multiplayer1
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object MultiplayerGame extends App {
  val playerInterpreter = PlayerConsoleInterpreter.andThen(IdToFuture)
  val matchmakingInterpreter = new MatchmakingServerInterpreter
  val gameInterpreter = new GameServerInterpreter
  val interpreter0: Multiplayer0 ~> Future =
    matchmakingInterpreter or gameInterpreter
  val interpreter1: Multiplayer1 ~> Future = playerInterpreter or interpreter0
  val interpreter: Multiplayer ~> Future = TimingInterpreter or interpreter1

  try {
    val gameResult = Multiplayer
      .program(
        new Player.Ops[Multiplayer],
        new Matchmaking.Ops[Multiplayer],
        new Game.Ops[Multiplayer],
        new Timing.Ops[Multiplayer]
      )
      .foldMap(interpreter)
    Await.result(gameResult, 360.seconds)
  } finally {
    matchmakingInterpreter.terminate()
    gameInterpreter.terminate()
  }
}
