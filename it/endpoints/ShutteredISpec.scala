package endpoints

import play.api.Application
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.libs.ws.WSResponse
import support.ISpec

import scala.concurrent.Future

class ShutteredISpec extends ISpec {

  override def fakeApplication: Application = appBuilder.configure("isShuttered" -> true).build()

  val get: String => Future[WSResponse] = request(_).get()
  val post: String => Future[WSResponse] = request(_).post(Map.empty[String, String])

  "an endpoint" when {
    Seq[(String, String => Future[WSResponse])](
      "/" -> get,
      "/search-by-nino" -> get,
      "/search-by-nino" -> post,
      "/search-by-passport" -> get,
      "/search-by-passport" -> post,
      "/status-result" -> get,
      "/foo" -> get
    ).foreach { case (path, request) =>
      s"path is $path and method is ${request.getClass.getName} and app is shuttered" should {
        "return the shutter page" in {
          val result: WSResponse = request(path).futureValue

          result.status shouldBe SERVICE_UNAVAILABLE
          result.body should include(htmlEscapedMessage("shuttering.title"))
        }
      }
    }
  }

}
