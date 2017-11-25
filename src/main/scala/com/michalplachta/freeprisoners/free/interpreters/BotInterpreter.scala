package com.michalplachta.freeprisoners.free.interpreters

import cats.{Id, ~>}
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.free.algebras.BotOps.{
  Bot,
  CreateBot,
  GetDecision
}

class BotInterpreter extends (Bot ~> Id) {
  var bots = Map.empty[Prisoner, Strategy]

  def apply[A](i: Bot[A]): Id[A] = i match {
    case CreateBot(name, strategy) =>
      val prisoner = Prisoner(name)
      bots += (prisoner -> strategy)
      prisoner

    case GetDecision(prisoner, otherPrisoner) =>
      bots.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence)
  }
}
