package com.michalplachta.freeprisoners.freestyle.programs.tools

import com.michalplachta.freeprisoners.freestyle.algebras.Timing
import freestyle.FreeS

import scala.concurrent.duration.FiniteDuration
import freestyle._

object Defer {
  def defer[S[_], A](program: FreeS[S, A], deferFor: FiniteDuration)(
      implicit timingOps: Timing[S]): FreeS[S, A] = {
    import timingOps._
    for {
      _ <- pause(deferFor)
      result <- program
    } yield result
  }
}
