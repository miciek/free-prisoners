package com.michalplachta.freeprisoners.apps

import com.michalplachta.freeprisoners.algebras.BotOps.Bot
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.interpreters.{
  BotInterpreter,
  PlayerConsoleInterpreter
}
import com.michalplachta.freeprisoners.programs.SinglePlayer
import com.michalplachta.freeprisoners.programs.SinglePlayer.SinglePlayer

object SinglePlayerGame extends App {
  SinglePlayer
    .program(
      new Player.Ops[SinglePlayer],
      new Bot.Ops[SinglePlayer]
    )
    .foldMap(PlayerConsoleInterpreter or BotInterpreter)
}
