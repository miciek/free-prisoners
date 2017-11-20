package com.michalplachta.freeprisoners.programs

import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.programs.Multiplayer.findOpponent
import com.michalplachta.freeprisoners.testinterpreters.GameTestInterpreter.GameState
import com.michalplachta.freeprisoners.testinterpreters.MatchmakingTestInterpreter.MatchmakingState
import com.michalplachta.freeprisoners.testinterpreters.PlayerGameTestInterpreter.{
  PlayerGame,
  PlayerGameState
}
import com.michalplachta.freeprisoners.testinterpreters.PlayerTestInterpreter.PlayerState
import com.michalplachta.freeprisoners.testinterpreters.{
  MatchmakingTestInterpreter,
  PlayerGameTestInterpreter
}
import org.scalatest.{Matchers, WordSpec}

class MultiplayerTest extends WordSpec with Matchers {
  "Multiplayer game" should {
    "have matchmaking module which" should {
      implicit val matchmakingOps: Matchmaking.Ops[Matchmaking] =
        new Matchmaking.Ops[Matchmaking]

      "be able to create a match when there is one opponent registered" in {
        val player = Prisoner("Player")
        val registeredOpponent = Prisoner("Opponent")

        val program: Free[Matchmaking, Option[Prisoner]] = for {
          _ <- matchmakingOps.registerAsWaiting(registeredOpponent)
          opponent <- findOpponent(player)
        } yield opponent

        val opponent: Option[Prisoner] = program
          .foldMap(new MatchmakingTestInterpreter)
          .runA(MatchmakingState.empty)
          .value

        opponent should contain(Prisoner("Opponent"))
      }

      "not be able to create a match when there are no opponents" in {
        val player = Prisoner("Player")

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(new MatchmakingTestInterpreter)
          .runA(MatchmakingState.empty)
          .value

        opponent should be(None)
      }

      "keep count of registered and unregistered players" in {
        val player = Prisoner("Player")

        val state: MatchmakingState = findOpponent(player)
          .foldMap(new MatchmakingTestInterpreter)
          .runS(MatchmakingState.empty)
          .value

        state.waitingPlayers.size should be(0)
        state.metPlayers should be(Set(player))
      }

      "wait for another player to join later" in {
        val player = Prisoner("Player")
        val lateJoiningOpponent = Prisoner("Opponent")

        val opponent: Option[Prisoner] = findOpponent(player)
          .foldMap(new MatchmakingTestInterpreter)
          .runA(
            MatchmakingState(waitingPlayers = Set.empty,
                             joiningPlayer = Some(lateJoiningOpponent),
                             metPlayers = Set.empty))
          .value

        opponent should contain(lateJoiningOpponent)
      }
    }

    "have game module which" should {
      "be able to produce verdict if both players make decisions" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map(opponent -> Silence)))
        val result: PlayerGameState = Multiplayer
          .playTheGame(player, opponent)(new Player.Ops[PlayerGame],
                                         new Game.Ops[PlayerGame])
          .foldMap(new PlayerGameTestInterpreter)
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
          .playTheGame(player, opponent)(new Player.Ops[PlayerGame],
                                         new Game.Ops[PlayerGame])
          .foldMap(new PlayerGameTestInterpreter)
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
          .playTheGame(player, opponent)(new Player.Ops[PlayerGame],
                                         new Game.Ops[PlayerGame])
          .foldMap(new PlayerGameTestInterpreter)
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
          .playTheGame(player, opponent)(new Player.Ops[PlayerGame],
                                         new Game.Ops[PlayerGame])
          .foldMap(new PlayerGameTestInterpreter)
          .runS(initialState)
          .value

        resultState.gameState.decisions.get(player) should be(None)
      }
    }
  }
}
