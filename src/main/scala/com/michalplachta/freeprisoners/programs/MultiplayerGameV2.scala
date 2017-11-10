package com.michalplachta.freeprisoners.programs

import cats.data.EitherK
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchOps.Match
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.algebras.PlayerOps.Player

import scala.concurrent.duration._

object MultiplayerGameV2 {
  type MatchmakingMatch[A] = EitherK[Matchmaking, Match, A]
  type Multiplayer[A] = EitherK[Player, MatchmakingMatch, A]

  def program(implicit playerOps: Player.Ops[Multiplayer],
              matchmakingOps: Matchmaking.Ops[Multiplayer],
              matchOps: Match.Ops[Multiplayer]): Free[Multiplayer, Unit] = {
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
      matchOps: Match.Ops[S]): Free[S, Unit] = {
    ???
  }
}
