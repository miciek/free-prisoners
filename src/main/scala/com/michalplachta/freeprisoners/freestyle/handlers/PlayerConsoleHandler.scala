package com.michalplachta.freeprisoners.freestyle.handlers

import cats.Id
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

  implicit val playerConsoleHandler = new Player.Handler[Id] {
    override def meetPrisoner(introduction: String) = {
      say(introduction)
      say(s"What's your name?")
      val name = hear()
      say(s"Hello, $name!")
      Prisoner(name)
    }

    override def questionPrisoner(prisoner: Prisoner,
                                  otherPrisoner: Prisoner) = {
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

    override def displayVerdict(prisoner: Prisoner, verdict: Verdict) = {
      say(s"Verdict for ${prisoner.name} is $verdict")
    }
  }
}
