package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.{Strategies, verdict}
import com.michalplachta.freeprisoners.freestyle.algebras.{Bot, Player}
import freestyle._

object SinglePlayer {
  @module trait Ops {
    val player: Player
    val bot: Bot
  }

  def program[F[_]](implicit ops: Ops[F]): FreeS[F, Unit] = {
    import ops.bot._
    import ops.player._
    for {
      playerPrisoner <- meetPrisoner("Welcome to Freestyle Single Player Game")
      botPrisoner <- createBot("WALL-E", Strategies.alwaysBlame)
      playerDecision <- questionPrisoner(playerPrisoner, botPrisoner)
      botDecision <- getDecision(botPrisoner, playerPrisoner)
      _ <- giveVerdict(playerPrisoner, verdict(playerDecision, botDecision))
      _ <- giveVerdict(botPrisoner, verdict(botDecision, playerDecision))
    } yield ()
  }
}
