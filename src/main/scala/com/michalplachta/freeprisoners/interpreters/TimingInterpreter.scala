package com.michalplachta.freeprisoners.interpreters

import cats.~>
import com.michalplachta.freeprisoners.algebras.TimingOps.{Pause, Timing}

import scala.concurrent.Future

class TimingInterpreter extends (Timing ~> Future) {
  def apply[A](timing: Timing[A]) = timing match {
    case Pause(duration) =>
      Thread.sleep(duration.toMillis)
      Future.successful(())
  }
}
