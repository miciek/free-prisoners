package com.michalplachta.freeprisoners.free.interpreters

import cats.effect.IO
import cats.~>
import com.michalplachta.freeprisoners.free.algebras.TimingOps.{Pause, Timing}

object TimingInterpreter extends (Timing ~> IO) {
  def apply[A](timing: Timing[A]) = timing match {
    case Pause(duration) =>
      IO { Thread.sleep(duration.toMillis) }
  }
}
