package com.michalplachta.freeprisoners.apps

import cats.data.EitherK
import cats.free.Free
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.Strategies
import com.michalplachta.freeprisoners.free.algebras.BotOps.{
  Bot,
  CreateBot,
  GetDecision
}
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.{
  GetOpponentDecision,
  MeetOpponent,
  Opponent
}
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.interpreters.{
  BotInterpreter,
  PlayerConsoleInterpreter
}
import com.michalplachta.freeprisoners.free.programs.{
  SinglePlayer,
  UnknownOpponent
}

object UnknownOpponentGame extends App {
  val p1: Free[UnknownOpponent.Ops, Unit] =
    UnknownOpponent
      .program(
        new Player.Ops[UnknownOpponent.Ops],
        new Opponent.Ops[UnknownOpponent.Ops]
      )

  val i1 = new (Player ~> SinglePlayer.Ops) {
    override def apply[A](fa: Player[A]) = EitherK.leftc(fa)
  }
  val i2 = new (Opponent ~> Bot) {
    override def apply[A](fa: Opponent[A]) = fa match {
      case MeetOpponent() =>
        CreateBot("Roman", Strategies.alwaysSilent)
      case GetOpponentDecision(prisoner, otherPrisoner) =>
        GetDecision(prisoner, otherPrisoner)
    }
  }
  val i3 = new (Bot ~> SinglePlayer.Ops) {
    override def apply[A](fa: Bot[A]) = EitherK.rightc(fa)
  }

  val p2: Free[SinglePlayer.Ops, Unit] =
    p1.compile[SinglePlayer.Ops](i1 or i2.andThen(i3))

  p2.foldMap(PlayerConsoleInterpreter or new BotInterpreter).unsafeRunSync()
}
