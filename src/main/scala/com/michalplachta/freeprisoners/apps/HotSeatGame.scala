package com.michalplachta.freeprisoners.apps

import cats.Id
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.interpreters.PlayerConsoleInterpreter
import com.michalplachta.freeprisoners.free.programs.{HotSeat => FreeHotSeat}
import com.michalplachta.freeprisoners.freestyle.programs.{
  HotSeat => FreestyleHotSeat
}
import com.michalplachta.freeprisoners.freestyle.handlers.PlayerConsoleHandler
import freestyle._
import freestyle.implicits._

object HotSeatGame extends App with PlayerConsoleHandler {
  FreeHotSeat
    .program(new Player.Ops[Player])
    .foldMap(PlayerConsoleInterpreter)

  FreestyleHotSeat.program
    .interpret[Id]
}
