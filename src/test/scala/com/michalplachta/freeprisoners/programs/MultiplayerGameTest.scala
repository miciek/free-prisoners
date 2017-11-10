package com.michalplachta.freeprisoners.programs

import cats.{Id, ~>}
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import org.scalatest.{Matchers, WordSpec}

class MultiplayerGameTest extends WordSpec with Matchers {
  "Multiplayer game" should {
    "be able to create a match when the is one opponent waiting" in new Environment(
      waiting = Seq(Prisoner("A")),
      willJoin = None) {
      val player = Prisoner("Player")
      val opponent: Option[Prisoner] = MultiplayerGameV2
        .findOpponent(player)(new Matchmaking.Ops[Matchmaking])
        .foldMap(interpreter)
      opponent should contain(Prisoner("A"))
    }

    "be able to create a match when there is one opponent that would like to join" in new Environment(
      waiting = Seq.empty,
      willJoin = Some(Prisoner("B"))) {
      val player = Prisoner("Player")
      val opponent: Option[Prisoner] = MultiplayerGameV2
        .findOpponent(player)(new Matchmaking.Ops[Matchmaking])
        .foldMap(interpreter)
      opponent should contain(Prisoner("B"))
    }

    "not be able to create a match when there are no opponents" in new Environment(
      waiting = Seq.empty,
      willJoin = None) {
      val player = Prisoner("Player")
      val opponent: Option[Prisoner] = MultiplayerGameV2
        .findOpponent(player)(new Matchmaking.Ops[Matchmaking])
        .foldMap(interpreter)
      opponent should be(None)
    }
  }

  class Environment(waiting: Seq[Prisoner], willJoin: Option[Prisoner]) {
    val interpreter = new (Matchmaking ~> Id) {
      def apply[A](matchmaking: Matchmaking[A]): Id[A] = matchmaking match {
        case GetWaitingOpponents() => waiting.indices.toSeq
        case JoinWaitingOpponent(waitingOpponentId) =>
          waiting.lift(waitingOpponentId)
        case WaitForOpponent(_) => willJoin
      }
    }
  }
}
