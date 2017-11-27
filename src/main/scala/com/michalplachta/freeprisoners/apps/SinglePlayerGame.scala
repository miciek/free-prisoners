package com.michalplachta.freeprisoners.apps

import com.michalplachta.freeprisoners.free.algebras.BotOps.Bot
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.interpreters.{
  BotInterpreter,
  PlayerConsoleInterpreter
}
import com.michalplachta.freeprisoners.free.programs.SinglePlayer

object SinglePlayerGame extends App {
  SinglePlayer
    .program(
      new Player.Ops[SinglePlayer.Ops],
      new Bot.Ops[SinglePlayer.Ops]
    )
    .foldMap(PlayerConsoleInterpreter or new BotInterpreter)
}
