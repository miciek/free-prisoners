package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.freestyle.algebras.{
  Game,
  Matchmaking,
  Player,
  Timing
}
import com.michalplachta.freeprisoners.freestyle.programs.Multiplayer.findOpponent
import com.michalplachta.freeprisoners.freestyle.testhandlers.{
  MatchmakingTestHandler,
  PlayerGameTestHandler,
  TimingTestHandler
}
import com.michalplachta.freeprisoners.states.{
  GameState,
  MatchmakingState,
  PlayerGameState,
  PlayerState
}
import com.michalplachta.freeprisoners.states.MatchmakingState.{
  DelayedPrisoner,
  MatchmakingStateA
}
import com.michalplachta.freeprisoners.states.PlayerGameState.PlayerGameStateA
import org.scalatest.{Matchers, WordSpec}
import freestyle._
import freestyle.implicits._

class MultiplayerTest
    extends WordSpec
    with Matchers
    with MatchmakingTestHandler
    with TimingTestHandler
    with PlayerGameTestHandler {
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

      "is able to create a match even when an opponent registers late" in {
        val player = Prisoner("Player")
        val registeredOpponent = DelayedPrisoner(Prisoner("Opponent"), 3)

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

    "have game module" which {
      @module trait PlayTheGame {
        val player: Player
        val game: Game
        val timing: Timing
      }

      "is able to produce verdict if both players make decisions" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map(opponent -> Silence)))
        val result: PlayerGameState = Multiplayer
          .playTheGame[PlayTheGame.Op](player, opponent)
          .interpret[PlayerGameStateA]
          .runS(initialState)
          .value

        result.playerState.verdicts.get(player) should contain(Verdict(0))
      }

      "is not able to produce verdict if the opponent doesn't make a decision" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map.empty))

        val result: PlayerGameState = Multiplayer
          .playTheGame[PlayTheGame.Op](player, opponent)
          .interpret[PlayerGameStateA]
          .runS(initialState)
          .value

        result.playerState.verdicts should be(Map.empty)
      }

      "is able to produce verdict if the opponent makes a decision after some time" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map(opponent -> Guilty), delayInCalls = 10))

        val result: PlayerGameState = Multiplayer
          .playTheGame[PlayTheGame.Op](player, opponent)
          .interpret[PlayerGameStateA]
          .runS(initialState)
          .value

        result.playerState.verdicts.get(player) should contain(Verdict(3))
      }
    }
  }
}
