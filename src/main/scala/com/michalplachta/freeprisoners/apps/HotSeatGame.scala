package com.michalplachta.freeprisoners.apps

import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.interpreters.PlayerConsoleInterpreter
import com.michalplachta.freeprisoners.free.programs.HotSeat

object HotSeatGame extends App {
  HotSeat
    .program(new Player.Ops[Player])
    .foldMap(PlayerConsoleInterpreter)
}
