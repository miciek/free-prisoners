package com.michalplachta.freeprisoners

import cats.data.EitherK
import cats.free.Free
import cats.implicits.catsStdInstancesForFuture
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.algebras.ServerOps.Server
import com.michalplachta.freeprisoners.interpreters.{
  IdToFuture,
  PlayerConsoleInterpreter,
  RemoteServerInterpreter
}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object MultiplayerGame extends App {
  type Multiplayer[A] = EitherK[Player, Server, A]

  def program(playerOps: Player.Ops[Multiplayer],
              serverOps: Server.Ops[Multiplayer]): Free[Multiplayer, Unit] = {
    import playerOps._
    import serverOps._
    for {
      player <- meetPrisoner("Welcome to Multiplayer Game")
      opponent <- getOpponentFor(player)
      playerDecision <- questionPrisoner(player, otherPrisoner = opponent)
      _ <- sendDecision(player, opponent, playerDecision)
      opponentDecision <- getDecision(opponent)
      _ <- displayVerdict(
        player,
        PrisonersDilemma.verdict(playerDecision, opponentDecision))
    } yield ()
  }

  val playerInterpreter = PlayerConsoleInterpreter.andThen(IdToFuture)
  val serverInterpreter = new RemoteServerInterpreter()
  try {
    val gameResult = program(
      new Player.Ops[Multiplayer],
      new Server.Ops[Multiplayer]
    ).foldMap(playerInterpreter or serverInterpreter)
    Await.result(gameResult, 60.seconds)
  } finally {
    serverInterpreter.terminate()
  }
}
