package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.verdict
import com.michalplachta.freeprisoners.freestyle.algebras.{Opponent, Player}
import freestyle._

/*_*/
object UnknownOpponent {
  @module trait Ops {
    val player: Player
    val opponent: Opponent
  }

  def program[F[_]](implicit ops: Ops[F]): FreeS[F, Unit] = {
    import ops.player._
    import ops.opponent._
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
