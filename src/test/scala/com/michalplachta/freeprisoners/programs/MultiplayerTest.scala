package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.programs.Multiplayer.findOpponent
import com.michalplachta.freeprisoners.testinterpreters.GameTestInterpreter.GameState
import com.michalplachta.freeprisoners.testinterpreters.MatchmakingTestInterpreter.{
  DelayedPrisoner,
  MatchmakingState,
  MatchmakingStateA
}
import com.michalplachta.freeprisoners.testinterpreters.PlayerGameTestInterpreter.{
  PlayerGame,
  PlayerGameState
}
import com.michalplachta.freeprisoners.testinterpreters.PlayerTestInterpreter.PlayerState
import com.michalplachta.freeprisoners.testinterpreters.{
  MatchmakingTestInterpreter,
  PlayerGameTestInterpreter,
  TimingTestInterpreter
}
import org.scalatest.{Matchers, WordSpec}

class MultiplayerTest extends WordSpec with Matchers {
  "Multiplayer game" should {
    "have matchmaking module which" should {
      type TimedMatchmaking[A] = EitherK[Timing, Matchmaking, A]
      implicit val matchmakingOps: Matchmaking.Ops[TimedMatchmaking] =
        new Matchmaking.Ops[TimedMatchmaking]

      implicit val timingOps: Timing.Ops[TimedMatchmaking] =
        new Timing.Ops[TimedMatchmaking]

      val interpreter: TimedMatchmaking ~> MatchmakingStateA =
        new TimingTestInterpreter[MatchmakingStateA] or new MatchmakingTestInterpreter

      "be able to create a match when there is one opponent registered" in {
        val player = Prisoner("Player")
        val registeredOpponent = DelayedPrisoner(Prisoner("Opponent"), 0)

        val initialState =
          MatchmakingState(waitingPlayers = Set(registeredOpponent),
                           joiningPlayer = None,
                           metPlayers = Set.empty)

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        opponent should contain(registeredOpponent.prisoner)
      }

      "be able to create a match even when an opponent registers late" in {
        val player = Prisoner("Player")
        val registeredOpponent = DelayedPrisoner(Prisoner("Opponent"), 10)

        val initialState =
          MatchmakingState(waitingPlayers = Set(registeredOpponent),
                           joiningPlayer = None,
                           metPlayers = Set.empty)

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        opponent should contain(registeredOpponent.prisoner)
      }

      "not be able to create a match when there are no opponents" in {
        val player = Prisoner("Player")

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(MatchmakingState.empty)
          .value

        opponent should be(None)
      }

      "keep count of registered and unregistered players" in {
        val player = Prisoner("Player")

        val state: MatchmakingState = findOpponent(player)
          .foldMap(interpreter)
          .runS(MatchmakingState.empty)
          .value

        state.waitingPlayers.size should be(0)
        state.metPlayers should be(Set(player))
      }

      "wait for another player to join" in {
        val player = Prisoner("Player")
        val joiningOpponent = DelayedPrisoner(Prisoner("Opponent"), 0)

        val initialState = MatchmakingState(waitingPlayers = Set.empty,
                                            joiningPlayer =
                                              Some(joiningOpponent),
                                            metPlayers = Set.empty)

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        opponent should contain(joiningOpponent.prisoner)
      }

      "wait for another player who joins late" in {
        val player = Prisoner("Player")
        val lateJoiningOpponent = DelayedPrisoner(Prisoner("Opponent"), 10)

        val initialState = MatchmakingState(waitingPlayers = Set.empty,
                                            joiningPlayer =
                                              Some(lateJoiningOpponent),
                                            metPlayers = Set.empty)

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        opponent should contain(lateJoiningOpponent.prisoner)
      }

      "allow the player to obtain the opponent name even if the opponent unregistered" in {
        val player = Prisoner("Player")
        val opponent = DelayedPrisoner(Prisoner("Opponent"), 0)

        val initialState = MatchmakingState(waitingPlayers = Set.empty,
                                            joiningPlayer = Some(opponent),
                                            metPlayers = Set.empty)

        val foundOpponent: Option[Prisoner] = findOpponent(player)
          .foldMap(interpreter)
          .runA(initialState)
          .value

        foundOpponent should contain(opponent.prisoner)
      }
    }

    "have game module which" should {
      implicit val playerOps = new Player.Ops[PlayerGame]
      implicit val gameOps = new Game.Ops[PlayerGame]
      implicit val timingOps = new Timing.Ops[PlayerGame]
      val interpreter = new PlayerGameTestInterpreter

      "be able to produce verdict if both players make decisions" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map(opponent -> Silence)))
        val result: PlayerGameState = Multiplayer
          .playTheGame(player, opponent)
          .foldMap(interpreter)
          .runS(initialState)
          .value

        result.playerState.verdicts.get(player) should contain(Verdict(0))
      }

      "be not able to produce verdict if the opponent doesn't make a decision" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map.empty))

        val result: PlayerGameState = Multiplayer
          .playTheGame(player, opponent)
          .foldMap(interpreter)
          .runS(initialState)
          .value

        result.playerState.verdicts should be(Map.empty)
      }

      "be able to produce verdict if the opponent makes a decision after some time" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map(opponent -> Guilty), delayInCalls = 10))

        val result: PlayerGameState = Multiplayer
          .playTheGame(player, opponent)
          .foldMap(interpreter)
          .runS(initialState)
          .value

        result.playerState.verdicts.get(player) should contain(Verdict(3))
      }

      "should clear the player's decision after the game" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map(opponent -> Guilty)))

        val resultState: PlayerGameState = Multiplayer
          .playTheGame(player, opponent)
          .foldMap(interpreter)
          .runS(initialState)
          .value

        resultState.gameState.decisions.get(player) should be(None)
      }
    }
  }
}
