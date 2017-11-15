package com.michalplachta.freeprisoners.testinterpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.algebras.PlayerOps.{
  DisplayVerdict,
  MeetPrisoner,
  Player,
  QuestionPrisoner
}
import com.michalplachta.freeprisoners.testinterpreters.PlayerTestInterpreter.PlayerStateA

class PlayerTestInterpreter extends (Player ~> PlayerStateA) {
  def apply[A](player: Player[A]): PlayerStateA[A] = player match {
    case MeetPrisoner(_) =>
      State { state =>
        val fakePrisoner = state.fakePrisoners.head
        val newState = state.copy(
          fakePrisoners = state.fakePrisoners.tail,
          playingPrisoners =
            state.playingPrisoners + (fakePrisoner.prisoner -> fakePrisoner.decision))
        (newState, fakePrisoner.prisoner)
      }
    case QuestionPrisoner(prisoner, otherPrisoner) =>
      State { state =>
        (state, state.playingPrisoners.getOrElse(prisoner, Silence))
      }
    case DisplayVerdict(prisoner, verdict) =>
      State { state =>
        (state.copy(verdicts = state.verdicts + (prisoner -> verdict)), ())
      }
  }
}

object PlayerTestInterpreter {
  final case class FakePrisoner(prisoner: Prisoner, decision: Decision)
  final case class PlayerState(fakePrisoners: Set[FakePrisoner],
                               playingPrisoners: Map[Prisoner, Decision],
                               verdicts: Map[Prisoner, Verdict])
  type PlayerStateA[A] = State[PlayerState, A]
}
