package com.michalplachta.freeprisoners.free.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{Strategies, verdict}
import com.michalplachta.freeprisoners.free.algebras.BotOps.Bot
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player

object SinglePlayer {
  type Ops[A] = EitherK[Player, Bot, A]

  def program(implicit playerOps: Player.Ops[Ops],
              botOps: Bot.Ops[Ops]): Free[Ops, Unit] = {
    import botOps._
    import playerOps._
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
