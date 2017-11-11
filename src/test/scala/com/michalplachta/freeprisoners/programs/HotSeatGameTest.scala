package com.michalplachta.freeprisoners.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.interpreters.PlayerTestInterpreter
import com.michalplachta.freeprisoners.interpreters.PlayerTestInterpreter.{
  FakePrisoner,
  GameState
}
import org.scalatest.{Matchers, WordSpec}

class HotSeatGameTest extends WordSpec with Matchers {
  "Hot seat game" should {
    "question 2 prisoners and give verdicts" in {
      val blamingPrisoner = FakePrisoner(Prisoner("Blaming"), Guilty)
      val silentPrisoner = FakePrisoner(Prisoner("Silent"), Silence)
      val inputState =
        GameState(Set(blamingPrisoner, silentPrisoner), Map.empty, Map.empty)

      val result: GameState = HotSeatGame
        .program(new Player.Ops[Player])
        .foldMap(new PlayerTestInterpreter)
        .runS(inputState)
        .value

      result.verdicts should be(
        Map(blamingPrisoner.prisoner -> Verdict(0),
            silentPrisoner.prisoner -> Verdict(3)))
    }
  }
}
