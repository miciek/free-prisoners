package com.michalplachta.freeprisoners.freestyle.handlers

import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.freestyle.algebras.Opponent

import scala.util.Random

trait BotStatefulHandler {
  val names = Array("Wall-E", "R2-D2", "Megatron", "T-800")
  val strategies = Array(Strategy(_ => Guilty), Strategy(_ => Silence))
  val r = new Random()

  var bots = Map.empty[Prisoner, Strategy]

  implicit val botStateHandler = new Opponent.Handler[IO] {
    def meetOpponent = IO {
      val n = r.nextInt(names.length)
      val prisoner = Prisoner(names(n))

      val s = r.nextInt(strategies.length)
      bots += (prisoner -> strategies(s))
      prisoner
    }

    def getOpponentDecision(prisoner: PrisonersDilemma.Prisoner,
                            otherPrisoner: PrisonersDilemma.Prisoner) = IO {
      bots.get(prisoner).map(_.f(otherPrisoner)).getOrElse(Silence)
    }
  }
}
