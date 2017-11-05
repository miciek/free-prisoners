package com.michalplachta.freeprisoners.interpreters

import cats.{Id, ~>}
import com.michalplachta.freeprisoners.PrisonersDilemma.{Prisoner, Silence}
import com.michalplachta.freeprisoners.algebras.BotDSL.{
  Bot,
  CreateBot,
  GetDecision,
  Strategy
}

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
