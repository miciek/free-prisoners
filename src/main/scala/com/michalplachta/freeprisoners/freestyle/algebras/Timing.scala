package com.michalplachta.freeprisoners.freestyle.algebras

import freestyle.free

import scala.concurrent.duration.FiniteDuration

@free trait Timing {
  def pause(duration: FiniteDuration): FS[Unit]
}
