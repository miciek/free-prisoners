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

object SinglePlayerApp extends App {
  @module trait UnknownOpponentM {
    val player: Player
    val opponent: Opponent
  }

  implicit val playerHandler = new PlayerConsoleHandler
  implicit val opponentHandler = new BotStatefulHandler
  UnknownOpponent
    .program[UnknownOpponentM.Op]
    .interpret[IO]
    .unsafeRunSync()
}
