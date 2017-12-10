package com.michalplachta.freeprisoners.free.programs

import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player.Ops

object HotSeat {
  def program(implicit playerOps: Ops[Player]): Free[Player, Unit] = {
    import playerOps._
    for {
      prisonerA <- meetPrisoner("Welcome to Free Hot Seat Game, Prisoner A!")
      prisonerB <- meetPrisoner("Welcome to Free Hot Seat Game, Prisoner B!")
      decisionA <- getPlayerDecision(prisonerA, prisonerB)
      decisionB <- getPlayerDecision(prisonerB, prisonerA)
      _ <- giveVerdict(prisonerA, verdict(decisionA, decisionB))
      _ <- giveVerdict(prisonerB, verdict(decisionB, decisionA))
    } yield ()
  }
}
