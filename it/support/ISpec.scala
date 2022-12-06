package support

import crypto.FormModelEncrypter
import mocks.MockSessionCookie
import models.{FormQueryModel, NinoSearchFormModel}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.Helpers.LOCATION
import repositories.SessionCacheRepository

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

trait ISpec extends BaseISpec with MockSessionCookie {
  val baseUrl: String = s"http://localhost:$port/check-immigration-status"

  lazy val wsClient: WSClient                     = app.injector.instanceOf[WSClient]
  lazy val cacheRepo: SessionCacheRepository      = inject[SessionCacheRepository]
  lazy val formModelEncrypter: FormModelEncrypter = inject[FormModelEncrypter]

  def setFormQuery(formModel: NinoSearchFormModel, sessionId: String): Unit = {
    val encryptedFormModel =
      formModelEncrypter.encryptSearchFormModel(formModel, sessionId, "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8=")
    val formQueryModel = FormQueryModel(sessionId, encryptedFormModel)
    Await.result(cacheRepo.set(formQueryModel).map(_ => ()), 5.seconds)
  }

  def request(path: String, sessionId: String = "123"): WSRequest =
    wsClient
      .url(s"$baseUrl$path")
      .withHttpHeaders(
        "X-Session-ID" -> sessionId
      )

  def requestWithSession(path: String, sessionId: String): WSRequest =
    request(path, sessionId)
      .withCookies(mockSessionCookie(sessionId))
      .withFollowRedirects(false)

  def extractHeaderLocation(wsResponse: WSResponse): Option[String] =
    wsResponse.headers.get(LOCATION).map(_.head)

}
