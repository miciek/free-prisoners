package com.michalplachta.freeprisoners.freestyle.handlers

import cats.effect.IO
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.freestyle.algebras.Player
import PlayerConsoleHandler._

class PlayerConsoleHandler extends Player.Handler[IO] {
  override def meetPrisoner(introduction: String) =
    for {
      _ <- say(introduction)
      _ <- say(s"What's your name?")
      name <- hear()
      _ <- say(s"Hello, $name!")
    } yield Prisoner(name)

  override def getPlayerDecision(prisoner: Prisoner, otherPrisoner: Prisoner) =
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

  override def giveVerdict(prisoner: Prisoner, verdict: Verdict) =
    say(s"Verdict for ${prisoner.name} is $verdict")
}

object PlayerConsoleHandler {
  def say(what: String): IO[Unit] = IO { println(what) }
  def hear(): IO[String] = IO { scala.io.StdIn.readLine() }
}
