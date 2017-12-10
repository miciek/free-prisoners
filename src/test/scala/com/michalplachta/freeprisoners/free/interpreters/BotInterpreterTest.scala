package com.michalplachta.freeprisoners.free.interpreters

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import org.scalatest.{Matchers, WordSpec}

class BotInterpreterTest extends WordSpec with Matchers {
  "Bot interpreter for Opponent Ops" should {
    val opponentOps = new Opponent.Ops[Opponent]
    import opponentOps._

    "be able to create 3 bots" in {
      val createThreeOpponents = for {
        opp1 <- meetOpponent()
        opp2 <- meetOpponent()
        opp3 <- meetOpponent()
      } yield Seq(opp1, opp2, opp3)

      val result: Seq[Prisoner] =
        createThreeOpponents
          .foldMap(new BotInterpreter)
          .unsafeRunSync()

      result.length should be(3)
    }

    "provide the same answer for the same game" in {
      val player = Prisoner("player")
      val getTwoDecisions = for {
        opp <- meetOpponent()
        decision1 <- getOpponentDecision(player, opp)
        decision2 <- getOpponentDecision(player, opp)
      } yield (decision1, decision2)

      val result: (Decision, Decision) =
        getTwoDecisions
          .foldMap(new BotInterpreter)
          .unsafeRunSync()

      result._1 should be(result._2)
    }
  }
}
