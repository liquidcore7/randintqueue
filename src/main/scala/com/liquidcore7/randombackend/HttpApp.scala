package com.liquidcore7.randombackend

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.actor.typed.scaladsl.adapter._
import akka.stream.Materializer
import com.liquidcore7.randombackend.actor.NumberQueueActor
import com.liquidcore7.randombackend.routes.NumberQueueRoutes
import com.liquidcore7.randombackend.service.NumberQueueService

import scala.util.{Failure, Success}


object HttpApp extends App {

  val rootBehavior = Behaviors.setup[Nothing] { context =>
    implicit val system: ActorSystem[Nothing] = context.system

    import system.executionContext

    val queueActor = context.spawnAnonymous(NumberQueueActor())
    context.watch(queueActor)

    val numberQueueService = new NumberQueueService(queueActor)
    val route = new NumberQueueRoutes(numberQueueService)

    val httpConfig = system.settings.config.getConfig("randombackend.http")
    val port = httpConfig.getInt("port")
    val host = httpConfig.getString("host")

    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    implicit lazy val materializer: Materializer = Materializer(context)

    val httpServer = Http()
      .bindAndHandle(route.routes, host, port)

    httpServer.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        classicSystem.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

    Behaviors.empty
  }

  val server = ActorSystem[Nothing](rootBehavior, "RandomBackendServer")

}
