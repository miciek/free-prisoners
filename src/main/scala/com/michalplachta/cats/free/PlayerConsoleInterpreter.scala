package com.michalplachta.cats.free

import cats.{ Id, ~> }
import com.michalplachta.cats.free.PlayerDSL.{ DisplayVerdict, MeetPrisoner, Player, QuestionPrisoner }
import com.michalplachta.cats.free.PrisonersDilemma.{ Guilty, Prisoner, Silence }

object PlayerConsoleInterpreter extends (Player ~> Id) {
  def say(what: String): Unit = println(what)
  def hear(): String = scala.io.StdIn.readLine()

  def apply[A](i: Player[A]): Id[A] = i match {
    case MeetPrisoner(introduction) ⇒
      say(introduction)
      say(s"What's your name?")
      val name = hear()
      say(s"Hello, $name!")
      Prisoner(name)

    case QuestionPrisoner(prisoner, otherPrisoner) ⇒
      say(s"${prisoner.name}, is ${otherPrisoner.name} guilty? (y if guilty, anything if silent)")
      val answer = hear()
      val decision = answer match {
        case "y" ⇒ Guilty
        case _   ⇒ Silence
      }
      say(s"Your decision: $decision")
      decision

    case DisplayVerdict(prisoner, verdict) ⇒
      say(s"Verdict for ${prisoner.name} is $verdict")
  }
}

