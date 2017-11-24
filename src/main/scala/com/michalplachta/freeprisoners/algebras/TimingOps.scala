package com.michalplachta.freeprisoners.algebras

import cats.:<:
import cats.free.Free

import scala.concurrent.duration.FiniteDuration

object TimingOps {
  sealed trait Timing[A]
  final case class Pause(duration: FiniteDuration) extends Timing[Unit]

  object Timing {
    class Ops[S[_]](implicit s: Timing :<: S) {
      def pause(duration: FiniteDuration): Free[S, Unit] =
        Free.inject(Pause(duration))
    }
  }
}
