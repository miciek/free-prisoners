package com.michalplachta.freeprisoners.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.interpreters.MatchmakingTestInterpreter
import org.scalatest.{Matchers, WordSpec}

class MultiplayerGameTest extends WordSpec with Matchers {
  "Multiplayer game" should {
    "have matchmaking module which" should {
      "be able to create a match when the is one opponent waiting" in {
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
  }
}
