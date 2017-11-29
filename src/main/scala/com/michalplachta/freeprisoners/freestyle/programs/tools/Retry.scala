package com.michalplachta.freeprisoners.freestyle.programs.tools

import freestyle.FreeS
import freestyle.FreeS.pure
import freestyle._

object Retry {
  def retry[S[_], A](program: FreeS[S, A],
                     until: A => Boolean,
                     maxRetries: Int): FreeS[S, A] = {
    def loop(retries: Int): FreeS[S, A] =
      for {
        possibleResult <- program
        result <- if (until(possibleResult) || retries <= 0)
          pure[S, A](possibleResult)
        else loop(retries - 1)
      } yield result
    loop(maxRetries)
  }
}
