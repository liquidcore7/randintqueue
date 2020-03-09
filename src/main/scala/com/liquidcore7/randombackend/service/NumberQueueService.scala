package com.liquidcore7.randombackend.service

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.liquidcore7.randombackend.actor.NumberQueueActor._

import scala.concurrent.{ExecutionContext, Future}


final class NumberQueueService(actor: ActorRef[NumberQueueRequest])
                              (implicit system: ActorSystem[_]) {

  implicit private val ec: ExecutionContext = system.executionContext
  implicit private val defaultTimeout: Timeout = Timeout.create(
    system.settings.config.getDuration("randombackend.routes.ask-timeout")
  )


  def nextRandom(min: Int, max: Int): Future[Int] = for {
    actorResponse <- actor.ask[NumberAnswer](PollHeadMessage(min, max, _))
  } yield actorResponse.value

  def clearQueue(): Future[Unit] = for {
    _ <- actor.ask[QueueClearedAnswer.type](ClearQueueMessage)
  } yield ()

  def getQueue: Future[List[Int]] = for {
    actorResponse <- actor.ask[NumbersAnswer](GetQueueMessage)
  } yield actorResponse.numbers

  def push(appendFront: List[Int]): Future[List[Int]] = for {
    actorResponse <- actor.ask[NumbersAnswer](AddNumbersMessage(appendFront, _))
  } yield actorResponse.numbers

}
