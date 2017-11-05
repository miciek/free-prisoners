package com.michalplachta.freeprisoners

import cats.free.Free
import com.michalplachta.freeprisoners.PlayerDSL.Player
import com.michalplachta.freeprisoners.PlayerDSL.Player.Ops

object HotSeatGame extends App {
  def program(playerOps: Ops[Player]): Free[Player, Unit] = {
    import playerOps._
    for {
      prisonerA <- meetPrisoner("Welcome to Hot Seat Game, Prisoner A!")
      prisonerB <- meetPrisoner("Welcome to Hot Seat Game, Prisoner B!")
      decisionA <- questionPrisoner(prisonerA, prisonerB)
      decisionB <- questionPrisoner(prisonerB, prisonerA)
      _ <- displayVerdict(prisonerA,
                          PrisonersDilemma.verdict(decisionA, decisionB))
      _ <- displayVerdict(prisonerB,
                          PrisonersDilemma.verdict(decisionB, decisionA))
    } yield ()
  }

  program(new Player.Ops[Player]).foldMap(PlayerConsoleInterpreter)
}
