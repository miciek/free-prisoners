package com.michalplachta.freeprisoners.free.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.testinterpreters.PlayerOpponentTestInterpreter
import com.michalplachta.freeprisoners.free.testinterpreters.PlayerOpponentTestInterpreter.PlayerOpponent
import com.michalplachta.freeprisoners.states.{
  FakePrisoner,
  OpponentState,
  PlayerOpponentState,
  PlayerState
}
import org.scalatest.{Matchers, WordSpec}

class SinglePlayerTest extends WordSpec with Matchers {
  "Single Player (Free) program" should {
    "question the player and give verdict" in {
      val player = FakePrisoner(Prisoner("Player"), Guilty)
      val inputState =
        PlayerOpponentState(PlayerState(Set(player), Map.empty, Map.empty),
                            OpponentState(Map.empty))

      val result: PlayerState = SinglePlayer
        .program(new Player.Ops[PlayerOpponent],
                 new Opponent.Ops[PlayerOpponent])
        .foldMap(new PlayerOpponentTestInterpreter)
        .runS(inputState)
        .value
        .playerState

      result.verdicts.get(player.prisoner) should contain(Verdict(3))
    }
  }
}
