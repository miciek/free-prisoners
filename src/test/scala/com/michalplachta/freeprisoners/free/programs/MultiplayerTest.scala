package com.michalplachta.freeprisoners.free.programs

import cats.data.EitherK
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.free.algebras.GameOps.Game
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.free.programs.Multiplayer.findOpponent
import com.michalplachta.freeprisoners.free.testinterpreters.{
  GameTestInterpreter,
  MatchmakingTestInterpreter,
  TimingTestInterpreter
}
import com.michalplachta.freeprisoners.states.MatchmakingState.{
  DelayedPrisoner,
  MatchmakingStateA
}
import com.michalplachta.freeprisoners.states.{GameState, MatchmakingState}
import org.scalatest.{Matchers, WordSpec}

class MultiplayerTest extends WordSpec with Matchers {
  "Multiplayer program" should {
    "have matchmaking module" which {
      type TimedMatchmaking[A] = EitherK[Timing, Matchmaking, A]
      implicit val matchmakingOps: Matchmaking.Ops[TimedMatchmaking] =
        new Matchmaking.Ops[TimedMatchmaking]

      implicit val timingOps: Timing.Ops[TimedMatchmaking] =
        new Timing.Ops[TimedMatchmaking]

      val interpreter: TimedMatchmaking ~> MatchmakingStateA =
        new TimingTestInterpreter[MatchmakingStateA] or new MatchmakingTestInterpreter

      "is able to create a match when there is one opponent registered" in {
        val player = Prisoner("Player")
        val registeredOpponent = DelayedPrisoner(Prisoner("Opponent"), 0)

        val initialState =
          MatchmakingState(waitingPlayers = List(registeredOpponent),
                           joiningPlayer = None,
                           metPlayers = Set.empty)

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        opponent should contain(registeredOpponent.prisoner)
      }

      "is not able to create a match when there are no opponents" in {
        val player = Prisoner("Player")

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(MatchmakingState.empty)
          .value

        opponent should be(None)
      }

      "keeps count of registered and unregistered players" in {
        val player = Prisoner("Player")

        val state: MatchmakingState = findOpponent(player)
          .foldMap(interpreter)
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

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
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

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        opponent should contain(lateJoiningOpponent.prisoner)
      }
    }

    "have decision registry module" which {
      type GameTiming[A] = EitherK[Game, Timing, A]
      implicit val gameOps = new Game.Ops[GameTiming]
      implicit val timingOps = new Timing.Ops[GameTiming]
      val interpreter = new GameTestInterpreter or new TimingTestInterpreter

      "is able to get opponent's decision" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map(opponent -> Silence))
        val result: Option[Decision] = Multiplayer
          .getRemoteOpponentDecision(opponent)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        result should contain(Silence)
      }

      "is able to get opponent's decision only once" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map(opponent -> Guilty))
        val getDecisionTwice = for {
          _ <- Multiplayer.getRemoteOpponentDecision(opponent)
          decision <- Multiplayer.getRemoteOpponentDecision(opponent)
        } yield decision

        val result: Option[Decision] =
          getDecisionTwice
            .foldMap(interpreter)
            .runA(initialState)
            .value

        result should be(None)
      }

      "is not able to get opponent's decision if he hasn't decided" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map.empty)
        val result: Option[Decision] = Multiplayer
          .getRemoteOpponentDecision(opponent)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        result should be(None)
      }

      "is able to get opponent's decision if he decides after some time" in {
        val opponent = Prisoner("Opponent")

        val initialState = GameState(Map(opponent -> Guilty), delayInCalls = 10)
        val result: Option[Decision] = Multiplayer
          .getRemoteOpponentDecision(opponent)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        result should contain(Guilty)
      }
    }
  }
}
