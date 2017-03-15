package com.michalplachta.cats.free

import cats.free.{ :<:, Free }
import com.michalplachta.cats.free.PrisonersDilemma.{ Decision, OtherPrisoner, Prisoner, Verdict }

object ServerDSL {
  sealed trait Server[A]
  final case class GetOpponentFor(prisoner: Prisoner) extends Server[Prisoner]
  final case class SendDecision(prisoner: Prisoner, otherPrisoner: OtherPrisoner, decision: Decision) extends Server[Verdict]

  object Server {
    class Ops[S[_]](implicit s: Server :<: S) {
      def getOpponentFor(prisoner: Prisoner): Free[S, Prisoner] =
        Free.liftF(s.inj(GetOpponentFor(prisoner)))
      def sendDecision(prisoner: Prisoner, otherPrisoner: OtherPrisoner, decision: Decision): Free[S, Verdict] =
        Free.liftF(s.inj(SendDecision(prisoner, otherPrisoner, decision)))
    }
  }
}
