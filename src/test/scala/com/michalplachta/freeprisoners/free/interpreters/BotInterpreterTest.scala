package com.michalplachta.freeprisoners.free.interpreters

import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import org.scalatest.{Matchers, WordSpec}

class BotInterpreterTest extends WordSpec with Matchers {
  "Bot interpreter for Opponent Ops" should {
    val opponentOps = new Opponent.Ops[Opponent]
    import opponentOps._

    "be able to create 3 bots" in {
      val player = Prisoner("Test Player")
      val createThreeOpponents = for {
        opp1 <- meetOpponent(player)
        opp2 <- meetOpponent(player)
        opp3 <- meetOpponent(player)
      } yield Seq(opp1, opp2, opp3)

      val result: Seq[Option[Prisoner]] =
        createThreeOpponents
          .foldMap(new BotInterpreter)
          .unsafeRunSync()

      result.count(_.isDefined) should be(3)
    }

    "provide the same answer for the same game" in {
      val player = Prisoner("Test Player")
      val getTwoDecisions = for {
        opp <- meetOpponent(player)
        decision1 <- getOpponentDecision(player, opp.get)
        decision2 <- getOpponentDecision(player, opp.get)
      } yield (decision1, decision2)

      val result: (Decision, Decision) =
        getTwoDecisions
          .foldMap(new BotInterpreter)
          .unsafeRunSync()

      result._1 should be(result._2)
    }
  }
}
