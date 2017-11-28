package com.michalplachta.freeprisoners.free.interpreters

import cats.effect.IO
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence
}
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.{
  GiveVerdict,
  MeetPrisoner,
  Player,
  QuestionPrisoner
}

object PlayerConsoleInterpreter extends (Player ~> IO) {
  def say(what: String): Unit = println(what)
  def hear(): String = scala.io.StdIn.readLine()

  def apply[A](i: Player[A]): IO[A] = i match {
    case MeetPrisoner(introduction) =>
      IO {
        say(introduction)
        say(s"What's your name?")
        val name = hear()
        say(s"Hello, $name!")
        Prisoner(name)
      }

    case QuestionPrisoner(prisoner, otherPrisoner) =>
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

    case GiveVerdict(prisoner, verdict) =>
      IO {
        say(s"Verdict for ${prisoner.name} is $verdict")
      }
  }
}
