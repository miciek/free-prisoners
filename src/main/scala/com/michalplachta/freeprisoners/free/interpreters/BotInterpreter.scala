package com.michalplachta.freeprisoners.free.interpreters

import cats.effect.IO
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Strategy
}
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.{
  GetOpponentDecision,
  MeetOpponent,
  Opponent
}

import scala.util.Random

class BotInterpreter extends (Opponent ~> IO) {
  val names = Array("Wall-E", "R2-D2", "Megatron", "T-800")
  val strategies = Array(Strategy(_ => Guilty), Strategy(_ => Silence))
  val r = new Random()
  var bots = Map.empty[Prisoner, Strategy]

  /*_*/
  def apply[A](i: Opponent[A]): IO[A] = i match {
    case MeetOpponent(_) =>
      IO {
        val prisoner = Prisoner(random(names))
        bots += (prisoner -> random(strategies))
        Some(prisoner)
      }

    case GetOpponentDecision(player, opponent) =>
      IO { bots.get(opponent).map(_.f(player)).getOrElse(Silence) }
  }

  private def random[T](array: Array[T]): T = {
    val i = r.nextInt(array.length)
    array(i)
  }
}
