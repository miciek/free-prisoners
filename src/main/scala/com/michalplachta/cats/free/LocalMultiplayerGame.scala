package com.michalplachta.cats.free

import cats.data.Coproduct
import cats.free.Free
import cats.implicits.catsStdInstancesForFuture
import cats.{ Id, ~> }
import com.michalplachta.cats.free.PlayerDSL.Player
import com.michalplachta.cats.free.ServerDSL.Server

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object LocalMultiplayerGame extends App {
  type Multiplayer[A] = Coproduct[Player, Server, A]
  def program(playerOps: Player.Ops[Multiplayer], serverOps: Server.Ops[Multiplayer]): Free[Multiplayer, Unit] = {
    import playerOps._, serverOps._
    for {
      player ← meetPrisoner("Welcome to Multiplayer Game")
      opponent ← getOpponentFor(player)
      playerDecision ← questionPrisoner(player, otherPrisoner = opponent)
      verdict ← sendDecision(player, opponent, playerDecision)
      _ ← displayVerdict(player, verdict)
    } yield ()
  }

  val idToFuture = new (Id ~> Future) {
    def apply[A](i: Id[A]): Future[A] = Future.successful(i)
  }

  try {
    val gameResult = program(
      new Player.Ops[Multiplayer],
      new Server.Ops[Multiplayer]
    ).foldMap(PlayerConsoleInterpreter.andThen(idToFuture) or LocalServerInterpreter)
    Await.result(gameResult, 60.seconds)
  } finally {
    LocalServerInterpreter.terminate()
  }
}
