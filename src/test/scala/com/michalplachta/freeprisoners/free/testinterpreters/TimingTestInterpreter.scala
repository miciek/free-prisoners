package com.michalplachta.freeprisoners.free.testinterpreters

import cats.{Applicative, ~>}
import com.michalplachta.freeprisoners.free.algebras.TimingOps.{Pause, Timing}

class TimingTestInterpreter[F[_]: Applicative] extends (Timing ~> F) {
  def apply[A](timing: Timing[A]) = timing match {
    case Pause(_) => Applicative[F].unit
  }
}
