package com.michalplachta.freeprisoners.actors

import akka.actor.ActorSelection
import akka.pattern.ask
import akka.util.Timeout
import cats.Eval.always
import cats.effect.IO

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.reflect.ClassTag

object ServerCommunication {
  def askServer[P[_], T: ClassTag](server: ActorSelection,
                                   message: P[T],
                                   maxRetries: Int = 0,
                                   retryTimeout: Timeout = Timeout(1.second))(
      implicit ec: ExecutionContext): IO[T] = {
    def loop(retries: Int): Future[T] = {
      val response = server.ask(message)(retryTimeout).mapTo[T]
      if (retries > 0) {
        response.recoverWith({ case _ => loop(retries - 1) })
      } else response
    }
    IO.fromFuture(always(loop(maxRetries)))
  }

  def tellServer[P[_]](server: ActorSelection, message: P[Unit]): IO[Unit] = {
    IO(server ! message)
  }
}
