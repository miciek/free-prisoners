package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.freestyle.algebras.Player
import com.michalplachta.freeprisoners.states.PlayerState.PlayerStateA

trait PlayerTestHandler {
  implicit val playerTestHandler = new Player.Handler[PlayerStateA] {
    override def meetPrisoner(introduction: String) = {
      State { state =>
        val fakePrisoner = state.fakePrisoners.head
        val newState = state.copy(
          fakePrisoners = state.fakePrisoners.tail,
          playingPrisoners =
            state.playingPrisoners + (fakePrisoner.prisoner -> fakePrisoner.decision))
        (newState, fakePrisoner.prisoner)
      }
    }

    override def questionPrisoner(prisoner: Prisoner,
                                  otherPrisoner: Prisoner) = {
      State { state =>
        (state, state.playingPrisoners.getOrElse(prisoner, Silence))
      }
    }

    override def giveVerdict(prisoner: Prisoner, verdict: Verdict) = {
      State { state =>
        (state.copy(verdicts = state.verdicts + (prisoner -> verdict)), ())
      }
    }
  }
}
