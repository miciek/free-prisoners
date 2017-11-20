package com.michalplachta.freeprisoners.testinterpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking.WaitingPlayer
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.testinterpreters.MatchmakingTestInterpreter.MatchmakingStateA

class MatchmakingTestInterpreter extends (Matchmaking ~> MatchmakingStateA) {
  def apply[A](matchmaking: Matchmaking[A]): MatchmakingStateA[A] =
    matchmaking match {
      case RegisterAsWaiting(player) =>
        State { state =>
          (state.copy(waitingPlayers = (state.waitingPlayers + player),
                      metPlayers = (state.metPlayers + player)),
           ())
        }
      case UnregisterPlayer(player) =>
        State { state =>
          (state.copy(waitingPlayers = (state.waitingPlayers - player)), ())
        }
      case GetWaitingPlayers() =>
        State { state =>
          if (state.delayWaitingPlayers <= 0) {
            (state, state.waitingPlayers.map(WaitingPlayer))
          } else
            (state.copy(delayWaitingPlayers = state.delayWaitingPlayers - 1),
             Set.empty)
        }
      case JoinWaitingPlayer(player, waitingPlayer) =>
        State { state =>
          (state, state.waitingPlayers.find(_ == waitingPlayer.prisoner))
        }
      case CheckIfOpponentJoined(_) =>
        State { state =>
          if (state.delayJoiningPlayer <= 0) {
            (state, state.joiningPlayer)
          } else {
            (state.copy(delayJoiningPlayer = state.delayJoiningPlayer - 1),
             None)
          }
        }
    }
}

object MatchmakingTestInterpreter {
  final case class MatchmakingState(waitingPlayers: Set[Prisoner],
                                    joiningPlayer: Option[Prisoner],
                                    metPlayers: Set[Prisoner],
                                    delayWaitingPlayers: Int = 0,
                                    delayJoiningPlayer: Int = 0)

  object MatchmakingState {
    val empty = MatchmakingState(Set.empty, None, Set.empty, 0, 0)
  }

  type MatchmakingStateA[A] = State[MatchmakingState, A]
}
