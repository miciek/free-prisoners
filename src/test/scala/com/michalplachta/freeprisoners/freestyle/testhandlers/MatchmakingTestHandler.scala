package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.implicits.catsStdInstancesForList
import cats.implicits.catsStdInstancesForOption
import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.freestyle.algebras.{
  Matchmaking,
  WaitingPlayer
}
import com.michalplachta.freeprisoners.states.MatchmakingState.{
  DelayedPrisoner,
  MatchmakingStateA,
  updateCalls
}

trait MatchmakingTestHandler {
  implicit val matchmakingTestHandler =
    new Matchmaking.Handler[MatchmakingStateA] {
      override def registerAsWaiting(player: Prisoner) = {
        State { state =>
          (state.copy(
             waitingPlayers = (DelayedPrisoner(player, 0) :: state.waitingPlayers),
             metPlayers = (state.metPlayers + player)),
           ())
        }
      }

      override def unregisterPlayer(player: Prisoner) = {
        State { state =>
          (state.copy(
             waitingPlayers =
               (state.waitingPlayers.filter(_ != DelayedPrisoner(player, 0)))),
           ())
        }
      }

      override def getWaitingPlayers = {
        State { state =>
          (state.copy(waitingPlayers = updateCalls(state.waitingPlayers)),
           state.waitingPlayers
             .filter(_.callsBeforeAvailable <= 0)
             .map(_.prisoner)
             .map(WaitingPlayer))
        }
      }

      override def joinWaitingPlayer(player: Prisoner,
                                     waitingPlayer: WaitingPlayer) = {
        State { state =>
          (state,
           state.waitingPlayers
             .find(_.prisoner == waitingPlayer.prisoner)
             .map(_.prisoner))
        }
      }

      override def checkIfOpponentJoined(player: Prisoner) = {
        State { state =>
          (state.copy(joiningPlayer = updateCalls(state.joiningPlayer)),
           state.joiningPlayer
             .filter(_.callsBeforeAvailable <= 0)
             .map(_.prisoner))
        }
      }
    }
}
