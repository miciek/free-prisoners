package com.michalplachta.freeprisoners.freestyle.handlers

import cats.effect.IO
import com.michalplachta.freeprisoners.freestyle.algebras.Timing

import scala.concurrent.duration.FiniteDuration

class TimingHandler extends Timing.Handler[IO] {
  override def pause(duration: FiniteDuration) = {
    IO { Thread.sleep(duration.toMillis) }
  }
}
