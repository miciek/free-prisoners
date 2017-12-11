package com.michalplachta.freeprisoners.free.programs.tools

import cats.free.Free
import com.michalplachta.freeprisoners.free.programs.tools.Defer.defer
import com.michalplachta.freeprisoners.free.algebras.TimingOps.Timing

import scala.concurrent.duration.FiniteDuration

class DeferredRetry(maxRetries: Int, durationBetweenRetries: FiniteDuration) {
  def retry[S[_], A](program: Free[S, A], until: A => Boolean)(
      implicit timingOps: Timing.Ops[S]): Free[S, A] = {
    Retry.retry[S, A](defer(program, durationBetweenRetries), until, maxRetries)
  }
}
