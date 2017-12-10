package com.michalplachta.freeprisoners.apps

import cats.effect.IO
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
    .program(new Player.Ops[Player]) // Free[Player, Unit]
    .foldMap(PlayerConsoleInterpreter) // IO[Unit]
    .unsafeRunSync() // Unit + 💥

  FreestyleHotSeat.program // FreeS[F, Unit]
    .interpret[IO] // IO[Unit]
    .unsafeRunSync() // Unit + 💥
}
