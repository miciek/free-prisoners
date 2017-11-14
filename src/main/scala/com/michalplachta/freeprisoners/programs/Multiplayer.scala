package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.free.Free
import cats.free.Free.pure
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player

import scala.concurrent.duration._

object Multiplayer {
  type MatchmakingMatch[A] = EitherK[Matchmaking, Game, A]
  type Multiplayer[A] = EitherK[Player, MatchmakingMatch, A]

  sealed trait GameResult
  case object GameFinishedSuccessfully extends GameResult
  case object NoDecisionFromOpponent extends GameResult

  def program(implicit playerOps: Player.Ops[Multiplayer],
              matchmakingOps: Matchmaking.Ops[Multiplayer],
              gameOps: Game.Ops[Multiplayer]): Free[Multiplayer, Unit] = {
    import playerOps._
    for {
      player <- meetPrisoner("Welcome to Multiplayer Game")
      maybeOpponent <- findOpponent(player)
      _ <- maybeOpponent.map(playTheGame(player, _)).getOrElse(program)
    } yield ()
  }

  def findOpponent[S[_]](player: Prisoner)(
      implicit matchmakingOps: Matchmaking.Ops[S])
    : Free[S, Option[Prisoner]] = {
    import matchmakingOps._
    for {
      _ <- registerAsWaiting(player)
      waitingPlayers <- getWaitingPlayers()
      opponent <- waitingPlayers
        .filterNot(_.prisoner == player)
        .headOption
        .map(joinWaitingPlayer)
        .getOrElse(pure[S, Option[Prisoner]](None))
      _ <- unregisterPlayer(player)
    } yield opponent
  }

  def playTheGame[S[_]](player: Prisoner, opponent: Prisoner)(
      implicit playerOps: Player.Ops[S],
      gameOps: Game.Ops[S]): Free[S, GameResult] = {
    import playerOps._
    import gameOps._
    for {
      decision <- questionPrisoner(player, opponent)
      _ <- sendDecision(player, opponent, decision)
      maybeOpponentDecision <- getOpponentDecision(player, opponent, 60.seconds)
      result <- maybeOpponentDecision match {
        case Some(opponentDecision) =>
          for {
            _ <- displayVerdict(
              player,
              PrisonersDilemma.verdict(decision, opponentDecision))
          } yield GameFinishedSuccessfully
        case None => pure[S, GameResult](NoDecisionFromOpponent)
      }
    } yield result
  }
}
