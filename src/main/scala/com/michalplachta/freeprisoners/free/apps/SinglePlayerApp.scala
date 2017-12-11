package com.michalplachta.freeprisoners.free.apps

import cats.data.EitherK
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.interpreters.{
  BotInterpreter,
  PlayerConsoleInterpreter
}
import com.michalplachta.freeprisoners.free.programs.UnknownOpponent

object SinglePlayerApp extends App {
  type UnknownOpponentOps[A] = EitherK[Player, Opponent, A]

  UnknownOpponent
    .program(
      new Player.Ops[UnknownOpponentOps],
      new Opponent.Ops[UnknownOpponentOps]
    ) // Free[UnknownOpponentOps, Unit]
    .foldMap(PlayerConsoleInterpreter or new BotInterpreter) // IO[Unit]
    .unsafeRunSync() // Unit + 💥
}
