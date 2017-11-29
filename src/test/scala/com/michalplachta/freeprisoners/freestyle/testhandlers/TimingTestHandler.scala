package com.michalplachta.freeprisoners.freestyle.testhandlers

import cats.Applicative
import com.michalplachta.freeprisoners.freestyle.algebras.Timing

import scala.concurrent.duration.FiniteDuration

trait TimingTestHandler {
  implicit def timingTestHandler[F[_]: Applicative] = new Timing.Handler[F] {
    override def pause(duration: FiniteDuration) =
      Applicative[F].unit
  }
}
