package com.michalplachta.freeprisoners.free.programs

import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.free.algebras.GameOps.Game
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.free.programs.tools.Defer.defer
import com.michalplachta.freeprisoners.free.programs.tools.Retry.retry

import scala.concurrent.duration._

object Multiplayer {
  def findOpponent[S[_]](player: Prisoner)(
      implicit matchmakingOps: Matchmaking.Ops[S],
      timingOps: Timing.Ops[S]): Free[S, Option[Prisoner]] = {
    import matchmakingOps._
    for {
      _ <- unregisterPlayer(player)
      waitingPlayers <- getWaitingPlayers()
      maybeOpponent <- waitingPlayers.headOption
        .map(joinWaitingPlayer(player, _))
        .getOrElse(waitForOpponent(player))
    } yield maybeOpponent
  }

  def waitForOpponent[S[_]](player: Prisoner)(
      implicit matchmakingOps: Matchmaking.Ops[S],
      timingOps: Timing.Ops[S]): Free[S, Option[Prisoner]] = {
    import matchmakingOps._
    for {
      _ <- registerAsWaiting(player)
      maybeOpponent <- retry[S, Option[Prisoner]](
        defer(checkIfOpponentJoined(player), 1.second),
        until = _.isDefined,
        maxRetries = 20)
      _ <- unregisterPlayer(player)
    } yield maybeOpponent
  }

  def getRemoteOpponentDecision[S[_]](opponent: Prisoner)(
      implicit gameOps: Game.Ops[S],
      timingOps: Timing.Ops[S]): Free[S, Option[Decision]] = {
    import gameOps._
    for {
      maybeOpponentDecision <- retry[S, Option[Decision]](
        defer(getRegisteredDecision(opponent), 1.second),
        until = _.isDefined,
        maxRetries = 100)
      _ <- clearRegisteredDecision(opponent)
    } yield maybeOpponentDecision
  }

}
