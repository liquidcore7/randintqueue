package com.liquidcore7.randombackend.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.util.Random


object NumberQueueActor {

  sealed trait NumberQueueRequest
  sealed trait NumberQueueResponse

  final case class  AddNumbersMessage(numbers: List[Int],
                                      replyTo: ActorRef[NumbersAnswer])           extends NumberQueueRequest
  final case class  PollHeadMessage  (min: Int,
                                      max: Int,
                                      replyTo: ActorRef[NumberAnswer])            extends NumberQueueRequest
  final case class  GetQueueMessage  (replyTo: ActorRef[NumbersAnswer])           extends NumberQueueRequest
  final case class  ClearQueueMessage(replyTo: ActorRef[QueueClearedAnswer.type]) extends NumberQueueRequest

  final case class  NumberAnswer     (value: Int)         extends NumberQueueResponse
  final case class  NumbersAnswer    (numbers: List[Int]) extends NumberQueueResponse
  final case object QueueClearedAnswer                    extends NumberQueueResponse


  def apply(): Behavior[NumberQueueRequest] = queue(Nil)

  private def queue(numberQueue: List[Int]): Behavior[NumberQueueRequest] = {
    Behaviors.receive[NumberQueueRequest] { (_, message) => message match {
      case AddNumbersMessage(newNumbers, replyTo) =>
        val newState = numberQueue ++ newNumbers
        replyTo ! NumbersAnswer(newState)
        queue(newState)

      case PollHeadMessage(min, max, replyTo)     =>
        val responseNumber = numberQueue.headOption.getOrElse(Random.between(min, max))
        replyTo ! NumberAnswer(responseNumber)
        queue(numberQueue drop 1)

      case GetQueueMessage(replyTo)               =>
        replyTo ! NumbersAnswer(numberQueue)
        Behaviors.same

      case ClearQueueMessage(replyTo)             =>
        replyTo ! QueueClearedAnswer
        queue(Nil)
    } }
  }
}
