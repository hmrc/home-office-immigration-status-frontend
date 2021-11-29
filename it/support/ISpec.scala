package support

import models.{FormQueryModel, StatusCheckByNinoFormModel}
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

  def setFormQuery(formModel: StatusCheckByNinoFormModel, sessionId: String) = {
    val encryptedFormModel = formModelEncrypter.encryptFormModel(formModel, sessionId, "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8=")
    val formQueryModel = FormQueryModel(sessionId, encryptedFormModel)
    val selector = Json.obj("_id" -> formQueryModel.id)
    val modifier = Json.obj("$set" -> (formQueryModel copy (lastUpdated = LocalDateTime.now)))
    cacheRepo.findAndUpdate(query = selector, update = modifier, upsert = true).map(_ => ())
  }

  def request(path: String, sessionId: String = "123"): WSRequest =
    wsClient
      .url(s"$baseUrl$path")
      .withHttpHeaders(
        "X-Session-ID" -> sessionId
      )

}
