package com.michalplachta.freeprisoners.freestyle.handlers

import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.freestyle.algebras.Bot

trait BotStatefulHandler {
  private var bots = Map.empty[Prisoner, Strategy]

  implicit val botStateHandler = new Bot.Handler[IO] {
    def createBot(name: String, strategy: PrisonersDilemma.Strategy) = {
      IO {
        val prisoner = Prisoner(name)
        bots = bots + (prisoner -> strategy)
        prisoner
      }
    }

    def getDecision(prisoner: PrisonersDilemma.Prisoner,
                    otherPrisoner: PrisonersDilemma.Prisoner) = {
      IO {
        bots.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence)
      }
    }
  }
}
