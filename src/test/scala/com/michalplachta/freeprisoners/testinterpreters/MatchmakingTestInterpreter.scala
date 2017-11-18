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
          (state, state.waitingPlayers.map(WaitingPlayer))
        }
      case JoinWaitingPlayer(player, waitingPlayer) =>
        State { state =>
          (state, state.waitingPlayers.find(_ == waitingPlayer.prisoner))
        }
      case CheckIfOpponentJoined(_) =>
        State { state =>
          (state, state.joiningPlayer)
        }
    }
}

object MatchmakingTestInterpreter {
  final case class MatchmakingState(waitingPlayers: Set[Prisoner],
                                    joiningPlayer: Option[Prisoner],
                                    metPlayers: Set[Prisoner])

  object MatchmakingState {
    val empty = MatchmakingState(Set.empty, None, Set.empty)
  }

  type MatchmakingStateA[A] = State[MatchmakingState, A]
}
