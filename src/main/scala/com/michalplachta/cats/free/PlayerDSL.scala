package com.michalplachta.cats.free

import cats.free.{ :<:, Free }
import com.michalplachta.cats.free.PrisonersDilemma.{ Decision, OtherPrisoner, Prisoner, Verdict }

import scala.language.higherKinds

object PlayerDSL {
  sealed trait Player[A]
  final case class MeetPrisoner(introduction: String) extends Player[Prisoner]
  final case class QuestionPrisoner(prisoner: Prisoner, otherPrisoner: OtherPrisoner) extends Player[Decision]
  final case class DisplayVerdict(prisoner: Prisoner, verdict: Verdict) extends Player[Unit]

  object Player {
    class Ops[S[_]](implicit s: Player :<: S) {
      def meetPrisoner(introduction: String): Free[S, Prisoner] =
        Free.liftF(s.inj(MeetPrisoner(introduction)))
      def questionPrisoner(prisoner: Prisoner, otherPrisoner: OtherPrisoner): Free[S, Decision] =
        Free.liftF(s.inj(QuestionPrisoner(prisoner, otherPrisoner)))
      def displayVerdict(prisoner: Prisoner, verdict: Verdict): Free[S, Unit] =
        Free.liftF(s.inj(DisplayVerdict(prisoner, verdict)))
    }

    object Ops {
      def apply[S[_]](implicit s: Player :<: S): Ops[S] = new Ops[S]
    }
  }
}
