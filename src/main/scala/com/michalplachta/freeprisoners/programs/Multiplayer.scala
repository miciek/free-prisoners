package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.free.Free
import cats.free.Free.pure
import com.michalplachta.freeprisoners.PrisonersDilemma.{Prisoner, verdict}
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking
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
      waitingPlayers <- getWaitingPlayers()
      opponent <- waitingPlayers
        .filterNot(_.prisoner == player)
        .headOption
        .map(joinWaitingPlayer(player, _))
        .getOrElse(checkIfOpponentJoined(player))
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
      maybeOpponentDecision <- getOpponentDecision(player, opponent)
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
}
