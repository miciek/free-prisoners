package com.michalplachta.freeprisoners.apps

import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.interpreters.PlayerConsoleInterpreter
import com.michalplachta.freeprisoners.programs.HotSeat

object HotSeatGame extends App {
  HotSeat
    .program(new Player.Ops[Player])
    .foldMap(PlayerConsoleInterpreter)
}
