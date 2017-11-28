package com.michalplachta.freeprisoners.freestyle.handlers

import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.freestyle.algebras.Player

trait PlayerConsoleHandler {
  private def say(what: String): Unit = println(what)
  private def hear(): String = scala.io.StdIn.readLine()

  implicit val playerIdHandler = new Player.Handler[IO] {
    override def meetPrisoner(introduction: String) = IO {
      say(introduction)
      say(s"What's your name?")
      val name = hear()
      say(s"Hello, $name!")
      Prisoner(name)
    }

    override def questionPrisoner(prisoner: Prisoner, otherPrisoner: Prisoner) =
      IO {
        say(
          s"${prisoner.name}, is ${otherPrisoner.name} guilty? (y if guilty, anything if silent)")
        val answer = hear()
        val decision = answer match {
          case "y" => Guilty
          case _   => Silence
        }
        say(s"Your decision: $decision")
        decision
      }

    override def giveVerdict(prisoner: Prisoner, verdict: Verdict) = {
      IO(say(s"Verdict for ${prisoner.name} is $verdict"))
    }
  }
}
