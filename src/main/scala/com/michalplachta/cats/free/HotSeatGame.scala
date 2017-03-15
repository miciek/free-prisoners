package com.michalplachta.cats.free

import cats.free.Free
import com.michalplachta.cats.free.PlayerDSL.Player

object HotSeatGame extends App {
  def program(playerOps: Player.Ops[Player]): Free[Player, Unit] = {
    import playerOps._
    for {
      prisonerA ← meetPrisoner("Welcome to Hot Seat Game, Prisoner A!")
      prisonerB ← meetPrisoner("Welcome to Hot Seat Game, Prisoner B!")
      decisionA ← questionPrisoner(prisonerA, prisonerB)
      decisionB ← questionPrisoner(prisonerB, prisonerA)
      _ ← displayVerdict(prisonerA, PrisonersDilemma.verdict(decisionA, decisionB))
      _ ← displayVerdict(prisonerB, PrisonersDilemma.verdict(decisionB, decisionA))
    } yield ()
  }

  program(new Player.Ops[Player]).foldMap(PlayerConsoleInterpreter)
}
