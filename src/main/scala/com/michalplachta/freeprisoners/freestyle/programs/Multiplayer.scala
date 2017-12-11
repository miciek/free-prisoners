package com.michalplachta.freeprisoners.freestyle.programs

import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}
import com.michalplachta.freeprisoners.freestyle.algebras._
import com.michalplachta.freeprisoners.freestyle.programs.tools.Defer.defer
import com.michalplachta.freeprisoners.freestyle.programs.tools.Retry.retry
import freestyle._

import scala.concurrent.duration._

object Multiplayer {
  def findOpponent[S[_]](player: Prisoner)(
      implicit matchmakingOps: Matchmaking[S],
      timingOps: Timing[S]): FreeS[S, Option[Prisoner]] = {
    import matchmakingOps._
    for {
      _ <- unregisterPlayer(player)
      waitingPlayers <- getWaitingPlayers()
      maybeOpponent <- waitingPlayers.headOption
        .map(joinWaitingPlayer(player, _).freeS)
        .getOrElse(waitForOpponent(player))
    } yield maybeOpponent
  }

  def waitForOpponent[S[_]](player: Prisoner)(
      implicit matchmakingOps: Matchmaking[S],
      timingOps: Timing[S]): FreeS[S, Option[Prisoner]] = {
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
      implicit gameOps: DecisionRegistry[S],
      timingOps: Timing[S]): FreeS[S, Option[Decision]] = {
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
