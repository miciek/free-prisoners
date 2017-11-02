package com.michalplachta.cats.free

import cats.{Id, ~>}
import com.michalplachta.cats.free.BotDSL.{
  Bot,
  CreateBot,
  GetDecision,
  Strategy
}
import com.michalplachta.cats.free.PrisonersDilemma.{Prisoner, Silence}

object BotInterpreter extends (Bot ~> Id) {
  var bots = Map.empty[Prisoner, Strategy]

  def apply[A](i: Bot[A]): Id[A] = i match {
    case CreateBot(name, strategy) =>
      val prisoner = Prisoner(name)
      bots += (prisoner -> strategy)
      prisoner

    case GetDecision(prisoner, otherPrisoner) =>
      bots.get(prisoner).map(_(otherPrisoner)).getOrElse(Silence)
  }
}
