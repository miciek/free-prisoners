package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  Prisoner,
  verdict
}
import com.michalplachta.freeprisoners.freestyle.algebras._
import com.michalplachta.freeprisoners.freestyle.programs.tools.Defer.defer
import com.michalplachta.freeprisoners.freestyle.programs.tools.Retry.retry
import freestyle.FreeS.pure
import freestyle.{FreeS, module}
import freestyle._

import scala.concurrent.duration._

object Multiplayer {
  sealed trait GameResult
  case object GameFinishedSuccessfully extends GameResult
  case object NoDecisionFromOpponent extends GameResult

  @module trait Ops {
    val player: Player
    val matchmaking: Matchmaking
    val game: Game
    val timing: Timing
  }

  def program[F[_]](implicit playerOps: Player[F],
                    matchmakingOps: Matchmaking[F],
                    gameOps: Game[F],
                    timingOps: Timing[F]): FreeS[F, Unit] = {
    import playerOps._
    for {
      player <- meetPrisoner("Welcome to Freestyle Multiplayer Game")
      maybeOpponent <- findOpponent(player)
      _ <- maybeOpponent.map(playTheGame(player, _)).getOrElse(program)
    } yield ()
  }

  def findOpponent[F[_]](player: Prisoner)(
      implicit matchmakingOps: Matchmaking[F],
      timingOps: Timing[F]): FreeS[F, Option[Prisoner]] = {
    import matchmakingOps._
    for {
      _ <- registerAsWaiting(player)
      waitingPlayers <- retry[F, List[WaitingPlayer]](
        defer(getWaitingPlayers(), 1.second),
        until = _.exists(_.prisoner != player),
        maxRetries = 5)
      opponent <- waitingPlayers
        .filterNot(_.prisoner == player)
        .headOption
        .map(joinWaitingPlayer(player, _).freeS)
        .getOrElse(
          retry[F, Option[Prisoner]](
            defer(checkIfOpponentJoined(player), 1.second),
            until = _.isDefined,
            maxRetries = 20))
      _ <- unregisterPlayer(player)
    } yield opponent
  }

  def playTheGame[F[_]](player: Prisoner, opponent: Prisoner)(
      implicit playerOps: Player[F],
      gameOps: Game[F],
      timingOps: Timing[F]): FreeS[F, GameResult] = {
    import playerOps._
    import gameOps._
    for {
      handle <- getGameHandle(player, opponent)
      decision <- getPlayerDecision(player, opponent)
      _ <- sendDecision(handle, player, decision)
      maybeOpponentDecision <- retry[F, Option[Decision]](
        defer(getOpponentDecision(handle, opponent), 1.second),
        until = _.isDefined,
        maxRetries = 100)
      result <- maybeOpponentDecision match {
        case Some(opponentDecision) =>
          for {
            _ <- giveVerdict(player, verdict(decision, opponentDecision)).freeS
          } yield GameFinishedSuccessfully
        case None => pure[F, GameResult](NoDecisionFromOpponent)
      }
    } yield result
  }
}
