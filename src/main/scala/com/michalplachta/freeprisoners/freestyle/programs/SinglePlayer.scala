package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.freestyle.algebras.{Opponent, Player}
import freestyle._

object SinglePlayer {
  @module trait Ops {
    val player: Player
    val opponent: Opponent
  }

  def program[F[_]](implicit ops: Ops[F]): FreeS[F, Unit] = {
    import ops.player._
    import ops.opponent._
    for {
      playerPrisoner <- meetPrisoner("Welcome to Freestyle Single Player Game")
      botPrisoner <- meetOpponent()
      playerDecision <- getPlayerDecision(playerPrisoner, botPrisoner)
      botDecision <- getOpponentDecision(botPrisoner, playerPrisoner)
      _ <- giveVerdict(playerPrisoner, verdict(playerDecision, botDecision))
      _ <- giveVerdict(botPrisoner, verdict(botDecision, playerDecision))
    } yield ()
  }
}
