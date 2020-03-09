package com.liquidcore7.randombackend.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import com.liquidcore7.randombackend.domain.IntList
import com.liquidcore7.randombackend.service.NumberQueueService
import de.heikoseeberger.akkahttpcirce.{BaseCirceSupport, FailFastUnmarshaller}


final class NumberQueueRoutes(numberQueueService: NumberQueueService)
                             (implicit system: ActorSystem[_]) extends BaseCirceSupport with FailFastUnmarshaller {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
  import io.circe.generic.auto._

  val routes: Route = cors() {
    path("next") {
      parameters('min.as[Int], 'max.as[Int]) { (min, max) =>
        onSuccess(numberQueueService.nextRandom(min, max)) { nextInt =>
          complete(nextInt)
        }
      }
    } ~ path("add") {
      post {
        entity(as[IntList]) { body =>
          onSuccess(numberQueueService.push(body.numbers)) { newQueue =>
            complete(IntList(newQueue))
          }
        }
      }
    } ~ path("clear") {
      post {
        onSuccess(numberQueueService.clearQueue()) {
          complete(StatusCodes.OK, HttpEntity.Empty)
        }
      }
    } ~ path("get") {
      get {
        onSuccess(numberQueueService.getQueue) { queueContent =>
          complete(IntList(queueContent))
        }
      }
    }
  }

}
