package com.michalplachta.freeprisoners.actors

import akka.actor.ActorSelection
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object ServerCommunication {
  def askServer[P[_], T: ClassTag](
      server: ActorSelection,
      message: P[T],
      maxRetries: Int,
      retryTimeout: Timeout)(implicit ec: ExecutionContext): Future[T] = {
    def loop(retries: Int = maxRetries): Future[T] = {
      val response = server.ask(message)(retryTimeout).mapTo[T]
      if (retries > 0) {
        response.recoverWith({ case _ => loop(retries - 1) })
      } else response
    }
    loop(maxRetries)
  }

  def tellServer[P[_]](server: ActorSelection, message: P[Unit])(
      implicit ec: ExecutionContext): Future[Unit] = {
    server ! message
    Future.successful(())
  }
}
