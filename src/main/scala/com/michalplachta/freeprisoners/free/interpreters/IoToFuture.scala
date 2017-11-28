package com.michalplachta.freeprisoners.free.interpreters

import cats.effect.IO
import cats.~>

import scala.concurrent.Future

object IoToFuture extends (IO ~> Future) {
  def apply[A](i: IO[A]): Future[A] = i.unsafeToFuture()
}
