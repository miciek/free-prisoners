package com.michalplachta.freeprisoners.programs

import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player.Ops

object HotSeat {
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
}
