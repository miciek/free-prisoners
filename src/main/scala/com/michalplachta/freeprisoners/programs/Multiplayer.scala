package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.free.Free
import cats.free.Free.pure
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  Prisoner,
  verdict
}
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking.WaitingPlayer
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player

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
      waitingPlayers <- retry[S, Set[WaitingPlayer]](getWaitingPlayers(),
                                                     until = _.nonEmpty,
                                                     maxRetries = 100)
      opponent <- waitingPlayers
        .filterNot(_.prisoner == player)
        .headOption
        .map(joinWaitingPlayer(player, _))
        .getOrElse(
          retry[S, Option[Prisoner]](checkIfOpponentJoined(player),
                                     until = _.isDefined,
                                     maxRetries = 100))
      _ <- unregisterPlayer(player)
    } yield opponent
  }

  def playTheGame[S[_]](player: Prisoner, opponent: Prisoner)(
      implicit playerOps: Player.Ops[S],
      gameOps: Game.Ops[S]): Free[S, GameResult] = {
    import gameOps._
    import playerOps._
    for {
      decision <- questionPrisoner(player, opponent)
      _ <- sendDecision(player, opponent, decision)
      maybeOpponentDecision <- retry[S, Option[Decision]](
        getOpponentDecision(player, opponent),
        until = _.isDefined,
        maxRetries = 100)
      result <- maybeOpponentDecision match {
        case Some(opponentDecision) =>
          for {
            _ <- displayVerdict(player, verdict(decision, opponentDecision))
          } yield GameFinishedSuccessfully
        case None => pure[S, GameResult](NoDecisionFromOpponent)
      }
      _ <- clearPlayerDecisions(player)
    } yield result
  }

  def retry[S[_], A](program: Free[S, A],
                     until: A => Boolean,
                     maxRetries: Int): Free[S, A] = {
    def loop(retries: Int): Free[S, A] =
      for {
        possibleResult <- program
        result <- if (until(possibleResult) || retries <= 0)
          pure[S, A](possibleResult)
        else loop(retries - 1)
      } yield result
    loop(maxRetries)
  }
}
