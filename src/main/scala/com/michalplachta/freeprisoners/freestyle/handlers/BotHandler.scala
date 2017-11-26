package com.michalplachta.freeprisoners.freestyle.handlers

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.freestyle.algebras.Bot
import com.michalplachta.freeprisoners.freestyle.handlers.BotHandler.BotStateA

trait BotHandler {
  implicit val botStateHandler = new Bot.Handler[BotStateA] {
    def createBot(name: String, strategy: PrisonersDilemma.Strategy) = {
      State { state =>
        val prisoner = Prisoner(name)
        (state.copy(bots = state.bots + (prisoner -> strategy)), prisoner)
      }
    }

    def getDecision(prisoner: PrisonersDilemma.Prisoner,
                    otherPrisoner: PrisonersDilemma.Prisoner) = {
      State.inspect(
        _.bots.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence))
    }
  }
}

object BotHandler {
  final case class BotState(bots: Map[Prisoner, Strategy])

  type BotStateA[A] = State[BotState, A]
}
