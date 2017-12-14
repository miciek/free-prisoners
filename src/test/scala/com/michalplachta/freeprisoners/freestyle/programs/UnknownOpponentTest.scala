package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.freestyle.algebras.{Opponent, Player}
import com.michalplachta.freeprisoners.freestyle.testhandlers.PlayerOpponentTestHandler
import com.michalplachta.freeprisoners.states.PlayerOpponentState.PlayerOpponentStateA
import com.michalplachta.freeprisoners.states.{
  FakePrisoner,
  OpponentState,
  PlayerOpponentState,
  PlayerState
}
import org.scalatest.{Matchers, WordSpec}
import freestyle._
import freestyle.implicits._

class UnknownOpponentTest
    extends WordSpec
    with Matchers
    with PlayerOpponentTestHandler {
  @module trait UnknownOpponentOps {
    val player: Player
    val opponent: Opponent
  }

  "Single Player (Freestyle) program" should {
    "question the player and give verdict" in {
      val player = FakePrisoner(Prisoner("Player"), Guilty)
      val inputState =
        PlayerOpponentState(PlayerState(Set(player), Map.empty, Map.empty),
                            OpponentState(Map.empty))

      val result: PlayerState = UnknownOpponent
        .program[UnknownOpponentOps.Op]
        .interpret[PlayerOpponentStateA]
        .runS(inputState)
        .value
        .playerState

      result.verdicts.get(player.prisoner) should contain(Verdict(3))
    }
  }
}
