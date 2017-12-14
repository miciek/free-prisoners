package com.michalplachta.freeprisoners.freestyle.apps

import cats.effect.IO
import com.michalplachta.freeprisoners.freestyle.algebras.{Opponent, Player}
import com.michalplachta.freeprisoners.freestyle.handlers.{
  BotStatefulHandler,
  PlayerConsoleHandler
}
import com.michalplachta.freeprisoners.freestyle.programs.UnknownOpponent
import freestyle._
import freestyle.implicits._

@module trait UnknownOpponentOps {
  val player: Player
  val opponent: Opponent
}

object SinglePlayerApp extends App {
  implicit val playerHandler = new PlayerConsoleHandler
  implicit val opponentHandler = new BotStatefulHandler
  UnknownOpponent
    .program[UnknownOpponentOps.Op] // FreeS[UnknownOpponentOps, Unit]
    .interpret[IO] // IO[Unit]
    .unsafeRunSync() // Unit + 💥
}
