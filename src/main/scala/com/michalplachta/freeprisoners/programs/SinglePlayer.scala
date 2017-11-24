package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.algebras.BotOps.{Bot, Strategies}
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player

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
      _ <- displayVerdict(playerPrisoner,
                          PrisonersDilemma.verdict(playerDecision, botDecision))
      _ <- displayVerdict(botPrisoner,
                          PrisonersDilemma.verdict(botDecision, playerDecision))
    } yield ()
  }
}
