package com.liquidcore7.randombackend.service

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterEach, Matchers}
import com.liquidcore7.randombackend.actor.NumberQueueActor

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


final class NumberQueueServiceSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterEach {

  private lazy val testKit = ActorTestKit()
  implicit private lazy val typedSystem = testKit.system

  private lazy val queueActor = testKit.spawn(NumberQueueActor())
  lazy val numberQueueService = new NumberQueueService(queueActor)


  "Number queue" should "support random number generation when empty" in {
    val min = 10
    val max = 100

    numberQueueService.nextRandom(min, max).map((min until max) should contain (_))
  }

  it should "support adding numbers" in {
    val fakeQueue = 1 :: 2 :: 4 :: Nil

    numberQueueService.push(fakeQueue).map(_ should contain theSameElementsInOrderAs fakeQueue)
  }

  it should "support fetching the queue" in {
    val content = 2 :: 4 :: 8 :: Nil

    for {
      _ <- numberQueueService.push(content)
      containedElements <- numberQueueService.getQueue
    } yield containedElements should contain theSameElementsInOrderAs content
  }

  it should "support clearing the queue" in {
    val content = (1 to 40).toList

    for {
      _ <- numberQueueService.push(content)
      _ <- numberQueueService.clearQueue()
      queueContent <- numberQueueService.getQueue
    } yield queueContent shouldBe empty
  }

  it should "poll the numbers from beginning" in {
    val fakeQueue = 2 :: 5 :: 3 :: Nil
    val min = fakeQueue.min
    val max = fakeQueue.max

    for {
      _ <- numberQueueService.push(fakeQueue)
      collected <- Future.traverse(fakeQueue) { _ => numberQueueService.nextRandom(min, max) }
    } yield collected should contain theSameElementsInOrderAs fakeQueue
  }

  it should "fallback to random number generation when empty" in {
    val fakeQueue = 2 :: 3 :: Nil
    val min = fakeQueue.min
    val max = 5

    for {
      _ <- numberQueueService.push(fakeQueue)
      collected <- Future.traverse(fakeQueue ++ (1 until 10)) { _ => numberQueueService.nextRandom(min, max) }
    } yield {
      collected should contain allElementsOf fakeQueue
      (min until max) should contain allElementsOf collected
    }
  }

  it should "append numbers to the end" in {
    val queue1 = (1 to 5).toList
    val queue2 = (7 to 10).toList

    val expected = queue1 ++ queue2

    for {
      _ <- numberQueueService.push(queue1)
      _ <- numberQueueService.push(queue2)

      collected <- numberQueueService.getQueue
    } yield collected should contain theSameElementsInOrderAs expected
  }

  override protected def beforeEach(): Unit = {
    Await.result(numberQueueService.clearQueue(), 1.seconds)
  }
}
