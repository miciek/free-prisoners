package com.michalplachta.freeprisoners.freestyle.handlers

import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.freestyle.algebras.Opponent

import scala.util.Random

class BotStatefulHandler extends Opponent.Handler[IO] {
  val names = Array("Wall-E", "R2-D2", "Megatron", "T-800")
  val strategies = Array(Strategy(_ => Guilty), Strategy(_ => Silence))
  val r = new Random()

  var bots = Map.empty[Prisoner, Strategy]

  def meetOpponent(player: Prisoner) = IO {
    val n = r.nextInt(names.length)
    val prisoner = Prisoner(names(n))

    val s = r.nextInt(strategies.length)
    bots += (prisoner -> strategies(s))
    Some(prisoner)
  }

  def getOpponentDecision(player: Prisoner, opponent: Prisoner) = IO {
    bots.get(opponent).map(_.f(player)).getOrElse(Silence)
  }
}
