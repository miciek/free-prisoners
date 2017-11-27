package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.freestyle.testhandlers.PlayerBotTestHandler
import com.michalplachta.freeprisoners.states.PlayerBotState.PlayerBotStateA
import com.michalplachta.freeprisoners.states.{
  BotState,
  FakePrisoner,
  PlayerBotState,
  PlayerState
}
import org.scalatest.{Matchers, WordSpec}
import freestyle._
import freestyle.implicits._

class SinglePlayerTest
    extends WordSpec
    with Matchers
    with PlayerBotTestHandler {
  "Single Player (Freestyle) program" should {
    "question the player and give verdict" in {
      val player = FakePrisoner(Prisoner("Player"), Guilty)
      val inputState =
        PlayerBotState(PlayerState(Set(player), Map.empty, Map.empty),
                       BotState(Map.empty))

      val result: PlayerState = SinglePlayer
        .program[SinglePlayer.Ops.Op]
        .interpret[PlayerBotStateA]
        .runS(inputState)
        .value
        .playerState

      result.verdicts.get(player.prisoner) should contain(Verdict(3))
    }
  }
}
