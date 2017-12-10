package com.michalplachta.freeprisoners.apps

import com.michalplachta.freeprisoners.PrisonersDilemma._

import scala.io.StdIn.readLine

object NaiveHotSeatGame extends App {
  def meetPrisoner(): Prisoner = {
    val name = readLine("Welcome to the Hot Seat Game! What's your name?")
    Prisoner(name)
  }
  def getPrisonersDecision(prisoner: Prisoner,
                           otherPrisoner: Prisoner): Decision = {
    val answer = readLine(s"${prisoner.name}, is ${otherPrisoner.name} guilty?")
    answer match {
      case "y" => Guilty
      case _   => Silence
    }
  }

  def verdict(prisonerDecision: Decision,
              otherPrisonerDecision: Decision): Verdict = {
    if (prisonerDecision == Silence && otherPrisonerDecision == Silence)
      Verdict(1)
    else if (prisonerDecision == Guilty && otherPrisonerDecision == Silence)
      Verdict(0)
    else
      Verdict(3)
  }

  def giveVerdict(prisoner: Prisoner, verdict: Verdict): Unit = {
    println(s"Verdict for ${prisoner.name} is $verdict")
  }

  val prisonerA = meetPrisoner()
  val prisonerB = meetPrisoner()
  val decisionA = getPrisonersDecision(prisonerA, prisonerB)
  val decisionB = getPrisonersDecision(prisonerB, prisonerA)
  giveVerdict(prisonerA, verdict(decisionA, decisionB))
  giveVerdict(prisonerB, verdict(decisionB, decisionA))
}
