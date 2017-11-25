package com.michalplachta.freeprisoners.free.programs

import cats.data.EitherK
import cats.free.Free
import cats.free.Free.pure
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  Prisoner,
  verdict
}
import com.michalplachta.freeprisoners.free.algebras.GameOps.Game
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking.WaitingPlayer
import com.michalplachta.freeprisoners.free.algebras.PlayerOps.Player
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing

import scala.concurrent.duration._

object Multiplayer {
  type Multiplayer0[A] = EitherK[Matchmaking, Game, A]
  type Multiplayer1[A] = EitherK[Player, Multiplayer0, A]
  type Multiplayer[A] = EitherK[Timing, Multiplayer1, A]

  sealed trait GameResult
  case object GameFinishedSuccessfully extends GameResult
  case object NoDecisionFromOpponent extends GameResult

  def program(implicit playerOps: Player.Ops[Multiplayer],
              matchmakingOps: Matchmaking.Ops[Multiplayer],
              gameOps: Game.Ops[Multiplayer],
              timingOps: Timing.Ops[Multiplayer]): Free[Multiplayer, Unit] = {
    import playerOps._
    for {
      player <- meetPrisoner("Welcome to Multiplayer Game")
      maybeOpponent <- findOpponent(player)
      _ <- maybeOpponent.map(playTheGame(player, _)).getOrElse(program)
    } yield ()
  }

  def findOpponent[S[_]](player: Prisoner)(
      implicit matchmakingOps: Matchmaking.Ops[S],
      timingOps: Timing.Ops[S]): Free[S, Option[Prisoner]] = {
    import matchmakingOps._
    for {
      _ <- registerAsWaiting(player)
      waitingPlayers <- retry[S, List[WaitingPlayer]](
        deferred(getWaitingPlayers(), 1.second),
        until = _.exists(_.prisoner != player),
        maxRetries = 5)
      opponent <- waitingPlayers
        .filterNot(_.prisoner == player)
        .headOption
        .map(joinWaitingPlayer(player, _))
        .getOrElse(
          retry[S, Option[Prisoner]](
            deferred(checkIfOpponentJoined(player), 1.second),
            until = _.isDefined,
            maxRetries = 20))
      _ <- unregisterPlayer(player)
    } yield opponent
  }

  def playTheGame[S[_]](player: Prisoner, opponent: Prisoner)(
      implicit playerOps: Player.Ops[S],
      gameOps: Game.Ops[S],
      timingOps: Timing.Ops[S]): Free[S, GameResult] = {
    import gameOps._
    import playerOps._
    for {
      handle <- getGameHandle(player, opponent)
      decision <- questionPrisoner(player, opponent)
      _ <- sendDecision(handle, player, decision)
      maybeOpponentDecision <- retry[S, Option[Decision]](
        deferred(getOpponentDecision(handle, opponent), 1.second),
        until = _.isDefined,
        maxRetries = 100)
      result <- maybeOpponentDecision match {
        case Some(opponentDecision) =>
          for {
            _ <- displayVerdict(player, verdict(decision, opponentDecision))
          } yield GameFinishedSuccessfully
        case None => pure[S, GameResult](NoDecisionFromOpponent)
      }
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

  def deferred[S[_], A](program: Free[S, A], deferFor: FiniteDuration)(
      implicit timingOps: Timing.Ops[S]): Free[S, A] = {
    import timingOps._
    for {
      _ <- pause(deferFor)
      result <- program
    } yield result
  }
}
