package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.freestyle.algebras.{Opponent, Player}
import freestyle._

/*_*/
object UnknownOpponent {
  def program[F[_]](implicit playerOps: Player[F],
                    opponentOps: Opponent[F]): FreeS[F, Unit] = {
    import playerOps._
    import opponentOps._
    for {
      player <- meetPrisoner("Welcome to Prisoner's Dilemma (Freestyle)")
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
