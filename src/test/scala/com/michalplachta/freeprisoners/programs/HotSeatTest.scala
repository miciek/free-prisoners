package com.michalplachta.freeprisoners.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.testinterpreters.PlayerTestInterpreter.{
  FakePrisoner,
  PlayerState
}
import com.michalplachta.freeprisoners.testinterpreters.PlayerTestInterpreter
import org.scalatest.{Matchers, WordSpec}

class HotSeatTest extends WordSpec with Matchers {
  "Hot Seat game" should {
    "question 2 prisoners and give verdicts" in {
      val blamingPrisoner = FakePrisoner(Prisoner("Blaming"), Guilty)
      val silentPrisoner = FakePrisoner(Prisoner("Silent"), Silence)
      val inputState =
        PlayerState(Set(blamingPrisoner, silentPrisoner), Map.empty, Map.empty)

      val result: PlayerState = HotSeat
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
