package com.michalplachta.freeprisoners.interpreters

import cats.data.State
import cats.~>
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.Matchmaking.WaitingPlayer
import com.michalplachta.freeprisoners.algebras.MatchmakingOps._
import com.michalplachta.freeprisoners.interpreters.MatchmakingTestInterpreter.MatchmakingStateA

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
      case JoinWaitingPlayer(player) =>
        State { state =>
          (state, state.waitingPlayers.find(_ == player.prisoner))
        }
    }
}

object MatchmakingTestInterpreter {
  final case class MatchmakingState(waitingPlayers: Set[Prisoner],
                                    metPlayers: Set[Prisoner])

  type MatchmakingStateA[A] = State[MatchmakingState, A]
}
