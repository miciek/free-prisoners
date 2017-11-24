package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free
import com.michalplachta.freeprisoners.PrisonersDilemma.{
  Decision,
  Prisoner,
  Verdict
}

object PlayerOps {
  sealed trait Player[A]
  final case class MeetPrisoner(introduction: String) extends Player[Prisoner]
  final case class QuestionPrisoner(prisoner: Prisoner, otherPrisoner: Prisoner)
      extends Player[Decision]
  final case class DisplayVerdict(prisoner: Prisoner, verdict: Verdict)
      extends Player[Unit]

  object Player {
    class Ops[S[_]](implicit s: Player :<: S) {
      def meetPrisoner(introduction: String): Free[S, Prisoner] =
        Free.inject(MeetPrisoner(introduction))

      def questionPrisoner(prisoner: Prisoner,
                           otherPrisoner: Prisoner): Free[S, Decision] =
        Free.inject(QuestionPrisoner(prisoner, otherPrisoner))

      def displayVerdict(prisoner: Prisoner, verdict: Verdict): Free[S, Unit] =
        Free.inject(DisplayVerdict(prisoner, verdict))
    }
  }
}
