package com.michalplachta.freeprisoners.interpreters

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
import com.michalplachta.freeprisoners.interpreters.PlayerTestInterpreter.GameStateA

class PlayerTestInterpreter extends (Player ~> GameStateA) {
  def apply[A](player: Player[A]): GameStateA[A] = player match {
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
  final case class GameState(fakePrisoners: Set[FakePrisoner],
                             playingPrisoners: Map[Prisoner, Decision],
                             verdicts: Map[Prisoner, Verdict])
  type GameStateA[A] = State[GameState, A]
}
