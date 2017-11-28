package com.michalplachta.freeprisoners.free.interpreters

import cats.effect.IO
import cats.~>
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

class BotInterpreter extends (Bot ~> IO) {
  var bots = Map.empty[Prisoner, Strategy]

  def apply[A](i: Bot[A]): IO[A] = i match {
    case CreateBot(name, strategy) =>
      IO {
        val prisoner = Prisoner(name)
        bots += (prisoner -> strategy)
        prisoner
      }

    case GetDecision(prisoner, otherPrisoner) =>
      IO {
        bots.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence)
      }
  }
}
