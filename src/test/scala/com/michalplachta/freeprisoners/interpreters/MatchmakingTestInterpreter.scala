package com.michalplachta.freeprisoners.interpreters

import cats.{Id, ~>}
import com.michalplachta.freeprisoners.PrisonersDilemma.Prisoner
import com.michalplachta.freeprisoners.algebras.MatchmakingOps.{
  GetWaitingOpponents,
  JoinWaitingOpponent,
  Matchmaking,
  WaitForOpponent
}

class MatchmakingTestInterpreter(waiting: Seq[Prisoner],
                                 willJoin: Option[Prisoner])
    extends (Matchmaking ~> Id) {
  def apply[A](matchmaking: Matchmaking[A]): Id[A] = matchmaking match {
    case GetWaitingOpponents() => waiting.indices
    case JoinWaitingOpponent(waitingOpponentId) =>
      waiting.lift(waitingOpponentId)
    case WaitForOpponent(_) => willJoin
  }
}
