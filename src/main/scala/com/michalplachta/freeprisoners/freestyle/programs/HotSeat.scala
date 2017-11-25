package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.freestyle.algebras.Player
import freestyle._

object HotSeat {
  def program[F[_]](implicit playerOps: Player[F]): FreeS[F, Unit] = {
    import playerOps._
    for {
      prisonerA <- meetPrisoner(
        "Welcome to Freestyle Hot Seat Game, Prisoner A!")
      prisonerB <- meetPrisoner(
        "Welcome to Freestyle Hot Seat Game, Prisoner B!")
      decisionA <- questionPrisoner(prisonerA, prisonerB)
      decisionB <- questionPrisoner(prisonerB, prisonerA)
      _ <- giveVerdict(prisonerA,
                       PrisonersDilemma.verdict(decisionA, decisionB))
      _ <- giveVerdict(prisonerB,
                       PrisonersDilemma.verdict(decisionB, decisionA))
    } yield ()
  }
}
