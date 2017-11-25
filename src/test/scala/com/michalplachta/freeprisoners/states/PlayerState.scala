package com.michalplachta.freeprisoners.states

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  Prisoner,
  Verdict
}

final case class FakePrisoner(prisoner: Prisoner, decision: Decision)
final case class PlayerState(fakePrisoners: Set[FakePrisoner],
                             playingPrisoners: Map[Prisoner, Decision],
                             verdicts: Map[Prisoner, Verdict])

object PlayerState {
  type PlayerStateA[A] = State[PlayerState, A]
}
