package com.michalplachta.cats.free

object PrisonersDilemma {
  final case class Prisoner(name: String)
  type OtherPrisoner = Prisoner

  sealed trait Decision
  case object Guilty extends Decision
  case object Silence extends Decision

  final case class Verdict(years: Int)

  def verdict(prisonerDecision: Decision, otherPrisonerDecision: Decision): Verdict = {
    if (prisonerDecision == Silence && otherPrisonerDecision == Silence)
      Verdict(1)
    else if (prisonerDecision == Guilty && otherPrisonerDecision == Silence)
      Verdict(0)
    else
      Verdict(3)
  }
}
