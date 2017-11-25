package com.michalplachta.freeprisoners.states

import cats.Functor
import cats.data.State
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.states.MatchmakingState.DelayedPrisoner

final case class MatchmakingState(waitingPlayers: List[DelayedPrisoner],
                                  joiningPlayer: Option[DelayedPrisoner],
                                  metPlayers: Set[Prisoner])

object MatchmakingState {
  final case class DelayedPrisoner(prisoner: Prisoner,
                                   callsBeforeAvailable: Int)

  type MatchmakingStateA[A] = State[MatchmakingState, A]

  val empty = MatchmakingState(List.empty, None, Set.empty)

  def updateCalls[F[_]: Functor](
      delayedPrisoners: F[DelayedPrisoner]): F[DelayedPrisoner] = {
    Functor[F].map(delayedPrisoners)(p =>
      p.copy(callsBeforeAvailable = Math.max(0, p.callsBeforeAvailable - 1)))
  }
}
