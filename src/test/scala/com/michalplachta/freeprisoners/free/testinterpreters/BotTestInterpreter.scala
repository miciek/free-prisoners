package com.michalplachta.freeprisoners.free.testinterpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{Prisoner, Silence}
import com.michalplachta.freeprisoners.free.algebras.BotOps.{
  Bot,
  CreateBot,
  GetDecision
}
import com.michalplachta.freeprisoners.states.BotHandler.BotStateA

class BotTestInterpreter extends (Bot ~> BotStateA) {
  override def apply[A](bot: Bot[A]) = bot match {
    case CreateBot(name, strategy) =>
      State { state =>
        val prisoner = Prisoner(name)
        (state.copy(bots = state.bots + (prisoner -> strategy)), prisoner)
      }
    case GetDecision(prisoner, otherPrisoner) =>
      State.inspect(
        _.bots.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence))
  }
}
