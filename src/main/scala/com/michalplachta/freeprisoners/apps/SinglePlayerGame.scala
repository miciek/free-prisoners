package com.michalplachta.freeprisoners.apps

import cats.effect.IO
import com.michalplachta.freeprisoners.free.algebras.BotOps.Bot
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.interpreters.{
  BotInterpreter,
  PlayerConsoleInterpreter
}
import com.michalplachta.freeprisoners.free.programs.{
  SinglePlayer => FreeSinglePlayer
}
import com.michalplachta.freeprisoners.freestyle.handlers.{
  BotStatefulHandler,
  PlayerConsoleHandler
}
import com.michalplachta.freeprisoners.freestyle.programs.{
  SinglePlayer => FreestyleSinglePlayer
}
import freestyle._
import freestyle.implicits._

object SinglePlayerGame
    extends App
    with PlayerConsoleHandler
    with BotStatefulHandler {
  FreeSinglePlayer
    .program(
      new Player.Ops[FreeSinglePlayer.Ops],
      new Bot.Ops[FreeSinglePlayer.Ops]
    )
    .foldMap(PlayerConsoleInterpreter or new BotInterpreter)
    .unsafeRunSync()

  FreestyleSinglePlayer
    .program[FreestyleSinglePlayer.Ops.Op]
    .interpret[IO]
    .unsafeRunSync()
}
