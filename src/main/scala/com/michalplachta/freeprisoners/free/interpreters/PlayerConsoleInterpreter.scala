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
  GetPrisonerDecision
}

object PlayerConsoleInterpreter extends (Player ~> IO) {
  def say(what: String): IO[Unit] = IO { println(what) }
  def hear(): IO[String] = IO { scala.io.StdIn.readLine() }

  /*_*/
  def apply[A](i: Player[A]): IO[A] = i match {
    case MeetPrisoner(introduction) =>
      for {
        _ <- say(introduction)
        _ <- say(s"What's your name?")
        name <- hear()
        _ <- say(s"Hello, $name!")
      } yield Prisoner(name)

    case GetPrisonerDecision(prisoner, otherPrisoner) =>
      for {
        _ <- say(
          s"${prisoner.name}, is ${otherPrisoner.name} guilty?" +
            s" (y if guilty, anything if silent)")
        answer <- hear()
        decision = answer match {
          case "y" => Guilty
          case _   => Silence
        }
        _ <- say(s"Your decision: $decision")
      } yield decision

    case GiveVerdict(prisoner, verdict) =>
      say(s"Verdict for ${prisoner.name} is $verdict")
  }
}
