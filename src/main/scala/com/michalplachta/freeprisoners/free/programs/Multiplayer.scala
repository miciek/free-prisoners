package com.michalplachta.freeprisoners.free.programs

import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma._
import com.michalplachta.freeprisoners.free.algebras.DecisionRegistryOps.DecisionRegistry
import com.michalplachta.freeprisoners.free.algebras.MatchmakingOps.Matchmaking
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing
import com.michalplachta.freeprisoners.free.programs.tools.DeferredRetry

import scala.concurrent.duration._

object Multiplayer {
  val deferredRetry = new DeferredRetry(100, 1.second)
  import deferredRetry._

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
      maybeOpponent <- retry[S, Option[Prisoner]](checkIfOpponentJoined(player),
                                                  until = _.isDefined)
      _ <- unregisterPlayer(player)
    } yield maybeOpponent
  }

  def getRemoteOpponentDecision[S[_]](opponent: Prisoner)(
      implicit gameOps: DecisionRegistry.Ops[S],
      timingOps: Timing.Ops[S]): Free[S, Option[Decision]] = {
    import gameOps._
    for {
      maybeOpponentDecision <- retry[S, Option[Decision]](
        getRegisteredDecision(opponent),
        until = _.isDefined)
      _ <- clearRegisteredDecision(opponent)
    } yield maybeOpponentDecision
  }
}
