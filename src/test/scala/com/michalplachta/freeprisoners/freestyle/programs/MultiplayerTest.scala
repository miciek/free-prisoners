package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.freestyle.algebras.{
  Game,
  Matchmaking,
  Timing
}
import com.michalplachta.freeprisoners.freestyle.programs.Multiplayer.findOpponent
import com.michalplachta.freeprisoners.freestyle.testhandlers.{
  GameTestHandler,
  MatchmakingTestHandler,
  TimingTestHandler
}
import com.michalplachta.freeprisoners.states.GameState.GameStateA
import com.michalplachta.freeprisoners.states.{GameState, MatchmakingState}
import com.michalplachta.freeprisoners.states.MatchmakingState.{
  DelayedPrisoner,
  MatchmakingStateA
}
import org.scalatest.{Matchers, WordSpec}
import freestyle._
import freestyle.implicits._

class MultiplayerTest
    extends WordSpec
    with Matchers
    with MatchmakingTestHandler
    with TimingTestHandler
    with GameTestHandler {
  "Multiplayer (Freestyle) program" should {
    "have matchmaking module" which {
      @module trait FindOpponent {
        val matchmaking: Matchmaking
        val timing: Timing
      }

      "is able to create a match when there is one opponent registered" in {
        val player = Prisoner("Player")
        val registeredOpponent = DelayedPrisoner(Prisoner("Opponent"), 0)

        val initialState =
          MatchmakingState(waitingPlayers = List(registeredOpponent),
                           joiningPlayer = None,
                           metPlayers = Set.empty)

        val opponent: Option[Prisoner] =
          findOpponent[FindOpponent.Op](player)
            .interpret[MatchmakingStateA]
            .runA(initialState)
            .value

        opponent should contain(registeredOpponent.prisoner)
      }

      "is not able to create a match when there are no opponents" in {
        val player = Prisoner("Player")

        val opponent: Option[Prisoner] =
          findOpponent[FindOpponent.Op](player)
            .interpret[MatchmakingStateA]
            .runA(MatchmakingState.empty)
            .value

        opponent should be(None)
      }

      "keeps count of registered and unregistered players" in {
        val player = Prisoner("Player")

        val state: MatchmakingState =
          findOpponent[FindOpponent.Op](player)
            .interpret[MatchmakingStateA]
            .runS(MatchmakingState.empty)
            .value

        state.waitingPlayers.size should be(0)
        state.metPlayers should be(Set(player))
      }

      "waits for another player to join" in {
        val player = Prisoner("Player")
        val joiningOpponent = DelayedPrisoner(Prisoner("Opponent"), 0)

        val initialState = MatchmakingState(waitingPlayers = List.empty,
                                            joiningPlayer =
                                              Some(joiningOpponent),
                                            metPlayers = Set.empty)

        val opponent: Option[Prisoner] =
          findOpponent[FindOpponent.Op](player)
            .interpret[MatchmakingStateA]
            .runA(initialState)
            .value

        opponent should contain(joiningOpponent.prisoner)
      }

      "waits for another player who joins late" in {
        val player = Prisoner("Player")
        val lateJoiningOpponent = DelayedPrisoner(Prisoner("Opponent"), 10)

        val initialState = MatchmakingState(waitingPlayers = List.empty,
                                            joiningPlayer =
                                              Some(lateJoiningOpponent),
                                            metPlayers = Set.empty)

        val opponent: Option[Prisoner] =
          findOpponent[FindOpponent.Op](player)
            .interpret[MatchmakingStateA]
            .runA(initialState)
            .value

        opponent should contain(lateJoiningOpponent.prisoner)
      }
    }

    "have decision registry module" which {
      @module trait GameTiming {
        val game: Game
        val timing: Timing
      }

      "is able to get opponent's decision" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map(opponent -> Silence))
        val result: Option[Decision] = Multiplayer
          .getRemoteOpponentDecision[GameTiming.Op](opponent)
          .interpret[GameStateA]
          .runA(initialState)
          .value

        result should contain(Silence)
      }

      "is able to get opponent's decision only once" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map(opponent -> Guilty))
        val getDecisionTwice = for {
          _ <- Multiplayer.getRemoteOpponentDecision[GameTiming.Op](opponent)
          decision <- Multiplayer.getRemoteOpponentDecision[GameTiming.Op](
            opponent)
        } yield decision

        val result: Option[Decision] =
          getDecisionTwice
            .interpret[GameStateA]
            .runA(initialState)
            .value

        result should be(None)
      }

      "is not able to get opponent's decision if he hasn't decided" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map.empty)
        val result: Option[Decision] = Multiplayer
          .getRemoteOpponentDecision[GameTiming.Op](opponent)
          .interpret[GameStateA]
          .runA(initialState)
          .value

        result should be(None)
      }

      "is able to get opponent's decision if he decides after some time" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map(opponent -> Guilty), delayInCalls = 10)
        val result: Option[Decision] = Multiplayer
          .getRemoteOpponentDecision[GameTiming.Op](opponent)
          .interpret[GameStateA]
          .runA(initialState)
          .value

        result should contain(Guilty)
      }
    }
  }
}
