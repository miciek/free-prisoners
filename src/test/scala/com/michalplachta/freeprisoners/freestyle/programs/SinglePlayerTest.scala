package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.freestyle.testhandlers.PlayerOpponentTestHandler
import com.michalplachta.freeprisoners.states.PlayerOpponentState.PlayerOpponentStateA
import com.michalplachta.freeprisoners.states.{
  OpponentState,
  FakePrisoner,
  PlayerOpponentState,
  PlayerState
}
import org.scalatest.{Matchers, WordSpec}
import freestyle._
import freestyle.implicits._

class SinglePlayerTest
    extends WordSpec
    with Matchers
    with PlayerOpponentTestHandler {
  "Single Player (Freestyle) program" should {
    "question the player and give verdict" in {
      val player = FakePrisoner(Prisoner("Player"), Guilty)
      val inputState =
        PlayerOpponentState(PlayerState(Set(player), Map.empty, Map.empty),
                            OpponentState(Map.empty))

      val result: PlayerState = SinglePlayer
        .program[SinglePlayer.Ops.Op]
        .interpret[PlayerOpponentStateA]
        .runS(inputState)
        .value
        .playerState

      result.verdicts.get(player.prisoner) should contain(Verdict(3))
    }
  }
}
