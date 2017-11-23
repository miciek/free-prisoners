package com.michalplachta.freeprisoners.testinterpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking.WaitingPlayer
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.testinterpreters.MatchmakingTestInterpreter.{
  DelayedPrisoner,
  MatchmakingStateA
}

class MatchmakingTestInterpreter extends (Matchmaking ~> MatchmakingStateA) {
  def apply[A](matchmaking: Matchmaking[A]): MatchmakingStateA[A] =
    matchmaking match {
      case RegisterAsWaiting(player) =>
        State { state =>
          (state.copy(waitingPlayers = (state.waitingPlayers +
                        DelayedPrisoner(player, 0)),
                      metPlayers = (state.metPlayers + player)),
           ())
        }
      case UnregisterPlayer(player) =>
        State { state =>
          (state.copy(
             waitingPlayers = (state.waitingPlayers -
               DelayedPrisoner(player, 0))),
           ())
        }
      case GetWaitingPlayers() =>
        State { state =>
          (state.copy(waitingPlayers = updateCalls(state.waitingPlayers)),
           state.waitingPlayers
             .filter(_.callsBeforeAvailable <= 0)
             .map(_.prisoner)
             .map(WaitingPlayer))
        }
      case JoinWaitingPlayer(player, waitingPlayer) =>
        State { state =>
          (state,
           state.waitingPlayers
             .find(_.prisoner == waitingPlayer.prisoner)
             .map(_.prisoner))
        }
      case CheckIfOpponentJoined(_) =>
        State { state =>
          (state.copy(joiningPlayer = updateCalls(state.joiningPlayer)),
           state.joiningPlayer
             .filter(_.callsBeforeAvailable <= 0)
             .map(_.prisoner))
        }
    }

  def updateCalls(fakePrisoners: Set[DelayedPrisoner]): Set[DelayedPrisoner] = {
    fakePrisoners.map(p =>
      p.copy(callsBeforeAvailable = Math.max(0, p.callsBeforeAvailable - 1)))
  }

  def updateCalls(
      fakePrisoners: Option[DelayedPrisoner]): Option[DelayedPrisoner] = {
    fakePrisoners.map(p =>
      p.copy(callsBeforeAvailable = Math.max(0, p.callsBeforeAvailable - 1)))
  }
}

object MatchmakingTestInterpreter {
  final case class DelayedPrisoner(prisoner: Prisoner,
                                   callsBeforeAvailable: Int)

  final case class MatchmakingState(waitingPlayers: Set[DelayedPrisoner],
                                    joiningPlayer: Option[DelayedPrisoner],
                                    metPlayers: Set[Prisoner])

  object MatchmakingState {
    val empty = MatchmakingState(Set.empty, None, Set.empty)
  }

  type MatchmakingStateA[A] = State[MatchmakingState, A]
}
