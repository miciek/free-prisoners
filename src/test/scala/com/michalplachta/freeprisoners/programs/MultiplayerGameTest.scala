package com.michalplachta.freeprisoners.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Guilty,
  Prisoner,
  Silence,
  Verdict
}
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.interpreters.GameTestInterpreter.GameState
import com.michalplachta.freeprisoners.interpreters.PlayerGameTestInterpreter.{
  PlayerGame,
  PlayerGameState
}
import com.michalplachta.freeprisoners.interpreters.PlayerTestInterpreter.PlayerState
import com.michalplachta.freeprisoners.interpreters.{
  MatchmakingTestInterpreter,
  PlayerGameTestInterpreter
}
import org.scalatest.{Matchers, WordSpec}

class MultiplayerGameTest extends WordSpec with Matchers {
  "Multiplayer game" should {
    "have matchmaking module which" should {
      "be able to create a match when there is one opponent waiting" in {
        val interpreter =
          new MatchmakingTestInterpreter(waiting = Seq(Prisoner("A")),
                                         willJoin = None)
        val player = Prisoner("Player")
        val opponent: Option[Prisoner] = MultiplayerGameV2
          .findOpponent(player)(new Matchmaking.Ops[Matchmaking])
          .foldMap(interpreter)
        opponent should contain(Prisoner("A"))
      }

      "be able to create a match when there is one opponent that would like to join" in {
        val interpreter =
          new MatchmakingTestInterpreter(waiting = Seq.empty,
                                         willJoin = Some(Prisoner("B")))
        val player = Prisoner("Player")
        val opponent: Option[Prisoner] = MultiplayerGameV2
          .findOpponent(player)(new Matchmaking.Ops[Matchmaking])
          .foldMap(interpreter)
        opponent should contain(Prisoner("B"))
      }

      "not be able to create a match when there are no opponents" in {
        val interpreter =
          new MatchmakingTestInterpreter(waiting = Seq.empty, willJoin = None)
        val player = Prisoner("Player")
        val opponent: Option[Prisoner] = MultiplayerGameV2
          .findOpponent(player)(new Matchmaking.Ops[Matchmaking])
          .foldMap(interpreter)
        opponent should be(None)
      }

    }

    "have game module which" should {
      "is able to produce verdict if both players make decisions" in {
        val player = Prisoner("Player")
        val opponent = Prisoner("Opponent")

        val initialState =
          PlayerGameState(PlayerState(Set.empty,
                                      Map(player -> Guilty),
                                      Map.empty),
                          GameState(Map(opponent -> Silence)))
        val result: PlayerGameState = MultiplayerGameV2
          .playTheGame(player, opponent)(new Player.Ops[PlayerGame],
                                         new Game.Ops[PlayerGame])
          .foldMap(new PlayerGameTestInterpreter)
          .runS(initialState)
          .value

        result.playerState.verdicts.get(player) should contain(Verdict(0))
      }
    }
  }
}
