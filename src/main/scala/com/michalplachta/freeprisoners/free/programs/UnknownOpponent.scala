package com.michalplachta.freeprisoners.free.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player

object UnknownOpponent {
  type Ops[A] = EitherK[Player, Opponent, A]

  def program(implicit playerOps: Player.Ops[Ops],
              opponentOps: Opponent.Ops[Ops]): Free[Ops, Unit] = {
    import opponentOps._
    import playerOps._
    for {
      playerPrisoner <- meetPrisoner("Welcome to Free Unknown Opponent Game")
      opponentPrisoner <- meetOpponent()
      playerDecision <- getPrisonerDecision(playerPrisoner, opponentPrisoner)
      opponentDecision <- getOpponentDecision(opponentPrisoner, playerPrisoner)
      _ <- giveVerdict(playerPrisoner,
                       verdict(playerDecision, opponentDecision))
    } yield ()
  }
}
