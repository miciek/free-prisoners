package com.michalplachta.freeprisoners.free.programs.tools

import cats.free.Free
import cats.free.Free.pure

object Retry {
  def retry[S[_], A](program: Free[S, A],
                     until: A => Boolean,
                     maxRetries: Int): Free[S, A] = {
    def loop(retries: Int): Free[S, A] =
      for {
        possibleResult <- program
        result <- if (until(possibleResult) || retries <= 0)
          pure[S, A](possibleResult)
        else loop(retries - 1)
      } yield result
    loop(maxRetries)
  }
}
