package support

import models.{FormQueryModel, NinoSearchFormModel}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{CookieHeaderEncoding, SessionCookieBaker}
import repositories.SessionCacheRepository
import crypto.FormModelEncrypter

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

trait ISpec extends BaseISpec {

  lazy val sessionCookieBaker: SessionCookieBaker = inject[SessionCookieBaker]
  lazy val cookieHeaderEncoding: CookieHeaderEncoding = inject[CookieHeaderEncoding]

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val baseUrl: String = s"http://localhost:$port/check-immigration-status"

  lazy val cacheRepo = inject[SessionCacheRepository]

  lazy val formModelEncrypter = inject[FormModelEncrypter]

  def setFormQuery(formModel: NinoSearchFormModel, sessionId: String) = {
    val encryptedFormModel = formModelEncrypter.encryptSearchFormModel(formModel, sessionId, "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8=")
    val formQueryModel = FormQueryModel(sessionId, encryptedFormModel)
    cacheRepo.set(formQueryModel).map(_ => ())
  }

  def request(path: String, sessionId: String = "123"): WSRequest =
    wsClient
      .url(s"$baseUrl$path")
      .withHttpHeaders(
        "X-Session-ID" -> sessionId
      )

}
