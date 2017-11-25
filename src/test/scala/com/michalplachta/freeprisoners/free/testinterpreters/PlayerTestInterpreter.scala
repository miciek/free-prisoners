package com.michalplachta.freeprisoners.free.testinterpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.Silence
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.{
  DisplayVerdict,
  MeetPrisoner,
  Player,
  QuestionPrisoner
}
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

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
