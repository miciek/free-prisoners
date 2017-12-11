package com.michalplachta.freeprisoners.free.programs

import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player

object UnknownOpponent {
  def program[S[_]](implicit playerOps: Player.Ops[S],
                    opponentOps: Opponent.Ops[S]): Free[S, Unit] = {
    import opponentOps._
    import playerOps._
    for {
      player <- meetPrisoner("Welcome to Prisoner's Dilemma (Free)")
      maybeOpponent <- meetOpponent(player)
      _ <- maybeOpponent
        .map(opponent => {
          for {
            playerDecision <- getPlayerDecision(player, opponent)
            opponentDecision <- getOpponentDecision(player, opponent)
            _ <- giveVerdict(player, verdict(playerDecision, opponentDecision))
          } yield ()
        })
        .getOrElse(program)
    } yield ()
  }
}
