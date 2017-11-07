package com.michalplachta.freeprisoners.programs

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.algebras.PlayerOps.{DisplayVerdict, MeetPrisoner, Player, QuestionPrisoner}
import com.michalplachta.freeprisoners.programs.HotSeatGameTest.{FakePrisoner, GameState, MockedEnvironment}
import org.scalatest.{Matchers, WordSpec}

class HotSeatGameTest extends WordSpec with Matchers {
  "Hot seat game" should {
    "question 2 prisoners and give verdicts" in new MockedEnvironment {
      val blamingPrisoner = FakePrisoner(Prisoner("Blaming"), Guilty)
      val silentPrisoner = FakePrisoner(Prisoner("Silent"), Silence)
      val inputState =
        GameState(Set(blamingPrisoner, silentPrisoner), Map.empty, Map.empty)

      val result: GameState = HotSeatGame
        .program(new Player.Ops[Player])
        .foldMap(playerInterpreter)
        .runS(inputState)
        .value

      result.verdicts should be(
        Map(blamingPrisoner.prisoner -> Verdict(0), silentPrisoner.prisoner -> Verdict(3)))
    }
  }

}

object HotSeatGameTest {
  case class FakePrisoner(prisoner: Prisoner, decision: Decision)
  case class GameState(fakePrisoners: Set[FakePrisoner],
                       playingPrisoners: Map[Prisoner, Decision],
                       verdicts: Map[Prisoner, Verdict])

  trait MockedEnvironment {
    type GameStateA[A] = State[GameState, A]
    val playerInterpreter = new (Player ~> GameStateA) {
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
  }
}
