package com.michalplachta.freeprisoners.free.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.free.algebras.BotOps.Bot
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.testinterpreters.PlayerBotTestInterpreter
import com.michalplachta.freeprisoners.free.testinterpreters.PlayerBotTestInterpreter.PlayerBot
import com.michalplachta.freeprisoners.states.{
  BotState,
  FakePrisoner,
  PlayerBotState,
  PlayerState
}
import org.scalatest.{Matchers, WordSpec}

class SinglePlayerTest extends WordSpec with Matchers {
  "Single Player (Free) program" should {
    "question the player and give verdict" in {
      val player = FakePrisoner(Prisoner("Player"), Guilty)
      val inputState =
        PlayerBotState(PlayerState(Set(player), Map.empty, Map.empty),
                       BotState(Map.empty))

      val result: PlayerState = SinglePlayer
        .program(new Player.Ops[PlayerBot], new Bot.Ops[PlayerBot])
        .foldMap(new PlayerBotTestInterpreter)
        .runS(inputState)
        .value
        .playerState

      result.verdicts.get(player.prisoner) should contain(Verdict(3)) // TODO: mock bot decision
    }
  }
}
