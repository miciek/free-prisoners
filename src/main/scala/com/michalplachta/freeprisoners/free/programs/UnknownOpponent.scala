package com.michalplachta.freeprisoners.free.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.free.algebras.OpponentOps.Opponent
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player

object UnknownOpponent {
  type Ops[A] = EitherK[Player, Opponent, A]

  def program(implicit playerOps: Player.Ops[Ops],
              opponentOps: Opponent.Ops[Ops]): Free[Ops, Unit] = {
    import opponentOps._
    import playerOps._
    for {
      player <- meetPrisoner("Welcome to Game vs Unknown Opponent (Free)")
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
