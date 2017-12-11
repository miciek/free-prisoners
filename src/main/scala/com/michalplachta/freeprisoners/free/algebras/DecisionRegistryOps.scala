package com.michalplachta.freeprisoners.free.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}

object DecisionRegistryOps {
  sealed trait DecisionRegistry[A]
  final case class RegisterDecision(prisoner: Prisoner, decision: Decision)
      extends DecisionRegistry[Unit]
  final case class GetRegisteredDecision(prisoner: Prisoner)
      extends DecisionRegistry[Option[Decision]]
  final case class ClearRegisteredDecision(prisoner: Prisoner)
      extends DecisionRegistry[Unit]

  object DecisionRegistry {
    class Ops[S[_]](implicit s: DecisionRegistry :<: S) {
      def registerDecision(prisoner: Prisoner,
                           decision: Decision): Free[S, Unit] =
        Free.inject(RegisterDecision(prisoner, decision))

      def getRegisteredDecision(prisoner: Prisoner): Free[S, Option[Decision]] =
        Free.inject(GetRegisteredDecision(prisoner))

      def clearRegisteredDecision(prisoner: Prisoner): Free[S, Unit] =
        Free.inject(ClearRegisteredDecision(prisoner))
    }
  }
}
