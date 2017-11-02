package com.michalplachta.cats.free

import cats.{Id, ~>}

import scala.concurrent.Future

object IdToFuture extends (Id ~> Future) {
  def apply[A](i: Id[A]): Future[A] = Future.successful(i)
}
