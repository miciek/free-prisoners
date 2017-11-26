package com.michalplachta.freeprisoners.freestyle

import com.michalplachta.freeprisoners.PrisonersDilemma.{Strategies, verdict}
import com.michalplachta.freeprisoners.freestyle.algebras.{Bot, Player}
import freestyle._

object SinglePlayer {
  @module trait Ops {
    val player: Player
    val bot: Bot
  }

  def program[F[_]](implicit ops: Ops[F]): FreeS[F, Unit] = {
    import ops.player._
    import ops.bot._
    for {
      playerPrisoner <- meetPrisoner("Welcome to Single Player Game")
      botPrisoner <- createBot("Romain", Strategies.alwaysBlame)
      playerDecision <- questionPrisoner(playerPrisoner, botPrisoner)
      botDecision <- getDecision(botPrisoner, playerPrisoner)
      _ <- giveVerdict(playerPrisoner, verdict(playerDecision, botDecision))
      _ <- giveVerdict(botPrisoner, verdict(botDecision, playerDecision))
    } yield ()
  }
}
