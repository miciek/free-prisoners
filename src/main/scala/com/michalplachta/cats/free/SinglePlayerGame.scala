package com.michalplachta.cats.free

import cats.data.Coproduct
import cats.free.Free
import com.michalplachta.cats.free.BotDSL.{ Bot, Strategies }
import com.michalplachta.cats.free.PlayerDSL.Player

object SinglePlayerGame extends App {
  type SinglePlayer[A] = Coproduct[Player, Bot, A]

  def program(playerOps: Player.Ops[SinglePlayer], botOps: Bot.Ops[SinglePlayer]): Free[SinglePlayer, Unit] = {
    import playerOps._, botOps._
    for {
      playerPrisoner ← meetPrisoner("Welcome to Single Player Game")
      botPrisoner ← createBot("Romain", Strategies.alwaysBlame)
      playerDecision ← questionPrisoner(playerPrisoner, botPrisoner)
      botDecision ← getDecision(botPrisoner, playerPrisoner)
      _ ← displayVerdict(playerPrisoner, PrisonersDilemma.verdict(playerDecision, botDecision))
      _ ← displayVerdict(botPrisoner, PrisonersDilemma.verdict(botDecision, playerDecision))
    } yield ()
  }

  program(
    new Player.Ops[SinglePlayer],
    new Bot.Ops[SinglePlayer]
  ).foldMap(PlayerConsoleInterpreter or BotInterpreter)
}
