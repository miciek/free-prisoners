package com.michalplachta.freeprisoners.free.programs

import cats.free.Free
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing

import scala.concurrent.duration.FiniteDuration

object Defer {
  def defer[S[_], A](program: Free[S, A], deferFor: FiniteDuration)(
      implicit timingOps: Timing.Ops[S]): Free[S, A] = {
    import timingOps._
    for {
      _ <- pause(deferFor)
      result <- program
    } yield result
  }
}
