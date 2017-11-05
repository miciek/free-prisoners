package com.michalplachta.cats.free

import cats.data.EitherK
import cats.free.Free
import cats.implicits.catsStdInstancesForFuture
import com.michalplachta.cats.free.PlayerDSL.Player
import com.michalplachta.cats.free.ServerDSL.Server

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object LocalMultiplayerGame extends App {
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

  try {
    val gameResult = program(
      new Player.Ops[Multiplayer],
      new Server.Ops[Multiplayer]
    ).foldMap(
      PlayerConsoleInterpreter.andThen(IdToFuture) or LocalServerInterpreter)
    Await.result(gameResult, 60.seconds)
  } finally {
    LocalServerInterpreter.terminate()
  }
}
