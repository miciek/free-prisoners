package com.michalplachta.freeprisoners.free.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{Strategies, verdict}
import com.michalplachta.freeprisoners.free.algebras.BotOps.Bot
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player

object SinglePlayer {
  type SinglePlayer[A] = EitherK[Player, Bot, A]

  def program(playerOps: Player.Ops[SinglePlayer],
              botOps: Bot.Ops[SinglePlayer]): Free[SinglePlayer, Unit] = {
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
