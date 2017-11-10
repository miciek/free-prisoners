package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  Prisoner,
  verdict
}
import com.michalplachta.freeprisoners.algebras.MatchOps.Match
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking.{
  MatchmakingResult,
  OpponentFound,
  OpponentNotFound
}
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player

import scala.concurrent.duration._

object MultiplayerGameV2 {
  type MatchmakingMatch[A] = EitherK[Matchmaking, Match, A]
  type Multiplayer[A] = EitherK[Player, MatchmakingMatch, A]

  def program(implicit playerOps: Player.Ops[Multiplayer],
              matchmakingOps: Matchmaking.Ops[Multiplayer],
              matchOps: Match.Ops[Multiplayer]): Free[Multiplayer, Unit] = {
    import playerOps._
    import matchOps._
    for {
      player <- meetPrisoner("Welcome to Multiplayer Game")
      opponent <- getOpponent(player)
      playerDecision <- questionPrisoner(player, otherPrisoner = opponent)
      _ <- sendDecision(player, opponent, playerDecision)
      opponentDecision <- waitForDecision(player, opponent)
      _ <- displayVerdict(player, verdict(playerDecision, opponentDecision))
    } yield ()
  }

  def getOpponent(player: Prisoner)(
      implicit matchmakingOps: Matchmaking.Ops[Multiplayer])
    : Free[Multiplayer, Prisoner] = {
    import matchmakingOps._
    for {
      maybeWaitingOpponent <- joinWaitingOpponent(player, 5.seconds)
      maybeOpponent <- maybeWaitingOpponent match {
        case result: OpponentFound =>
          Free.pure[Multiplayer, MatchmakingResult](result)
        case OpponentNotFound => waitForOpponent(player, 13.seconds)
      }
      opponent <- maybeOpponent match {
        case OpponentFound(opponent) =>
          Free.pure[Multiplayer, Prisoner](opponent)
        case OpponentNotFound => getOpponent(player)
      }
    } yield opponent
  }

  def waitForDecision(player: Prisoner, opponent: Prisoner)(
      implicit matchOps: Match.Ops[Multiplayer])
    : Free[Multiplayer, Decision] = {
    import matchOps._
    for {
      maybeDecision <- getOpponentDecision(player, opponent, 5.seconds)
      decision <- maybeDecision
        .map(Free.pure[Multiplayer, Decision])
        .getOrElse(waitForDecision(player, opponent))
    } yield decision
  }
}
