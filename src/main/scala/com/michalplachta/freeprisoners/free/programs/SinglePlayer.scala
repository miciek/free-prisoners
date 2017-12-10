package com.michalplachta.freeprisoners.free.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player

object SinglePlayer {
  type Ops[A] = EitherK[Player, Opponent, A]

  def program(implicit playerOps: Player.Ops[Ops],
              opponentOps: Opponent.Ops[Ops]): Free[Ops, Unit] = {
    import opponentOps._
    import playerOps._
    for {
      playerPrisoner <- meetPrisoner("Welcome to Free Single Player Game")
      botPrisoner <- meetOpponent()
      playerDecision <- getPlayerDecision(playerPrisoner, botPrisoner)
      botDecision <- getOpponentDecision(botPrisoner, playerPrisoner)
      _ <- giveVerdict(playerPrisoner, verdict(playerDecision, botDecision))
      _ <- giveVerdict(botPrisoner, verdict(botDecision, playerDecision))
    } yield ()
  }
}
