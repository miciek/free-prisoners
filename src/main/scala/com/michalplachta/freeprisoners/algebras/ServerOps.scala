package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  OtherPrisoner,
  Prisoner
}

object ServerOps {
  sealed trait Server[A]
  final case class GetOpponentFor(prisoner: Prisoner) extends Server[Prisoner]

  final case class SendDecision(prisoner: Prisoner,
                                otherPrisoner: OtherPrisoner,
                                decision: Decision)
      extends Server[Unit]

  final case class GetDecision(prisoner: Prisoner) extends Server[Decision]

  object Server {
    class Ops[S[_]](implicit s: Server :<: S) {
      def getOpponentFor(prisoner: Prisoner): Free[S, Prisoner] =
        Free.liftF(s.inj(GetOpponentFor(prisoner)))

      def sendDecision(prisoner: Prisoner,
                       otherPrisoner: OtherPrisoner,
                       decision: Decision): Free[S, Unit] =
        Free.liftF(s.inj(SendDecision(prisoner, otherPrisoner, decision)))

      def getDecision(prisoner: Prisoner): Free[S, Decision] =
        Free.liftF(s.inj(GetDecision(prisoner)))
    }
  }
}
