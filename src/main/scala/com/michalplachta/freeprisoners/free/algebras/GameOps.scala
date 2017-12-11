package com.michalplachta.freeprisoners.free.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{Decision, Prisoner}

object GameOps {
  sealed trait Game[A]
  final case class RegisterDecision(prisoner: Prisoner, decision: Decision)
      extends Game[Unit]
  final case class GetRegisteredDecision(prisoner: Prisoner)
      extends Game[Option[Decision]]
  final case class ClearRegisteredDecision(prisoner: Prisoner)
      extends Game[Unit]

  object Game {
    class Ops[S[_]](implicit s: Game :<: S) {
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
