package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.GameOps.Game
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player

import scala.concurrent.duration._

object MultiplayerGameV2 {
  type MatchmakingMatch[A] = EitherK[Matchmaking, Game, A]
  type Multiplayer[A] = EitherK[Player, MatchmakingMatch, A]

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
      waitingOpponents <- getWaitingOpponents()
      opponent <- waitingOpponents.headOption
        .map(joinWaitingOpponent)
        .getOrElse(waitForOpponent(60.seconds))
    } yield opponent
  }

  def playTheGame[S[_]](player: Prisoner, opponent: Prisoner)(
      implicit playerOps: Player.Ops[S],
      gameOps: Game.Ops[S]): Free[S, Unit] = {
    import playerOps._
    import gameOps._
    for {
      decision <- questionPrisoner(player, opponent)
      _ <- sendDecision(player, opponent, decision)
      opponentDecision <- getOpponentDecision(player, opponent, 60.seconds)
      _ <- displayVerdict(player,
                          PrisonersDilemma.verdict(decision, opponentDecision))
    } yield ()
  }
}
