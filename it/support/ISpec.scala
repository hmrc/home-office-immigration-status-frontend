package support

import models.{FormQueryModel, NinoSearchFormModel}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{CookieHeaderEncoding, SessionCookieBaker}
import repositories.SessionCacheRepository

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

trait ISpec extends BaseISpec {

  lazy val sessionCookieBaker: SessionCookieBaker = inject[SessionCookieBaker]
  lazy val cookieHeaderEncoding: CookieHeaderEncoding = inject[CookieHeaderEncoding]

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val baseUrl: String = s"http://localhost:$port/check-immigration-status"

  lazy val cacheRepo = inject[SessionCacheRepository]

  def setFormQuery(formModel: NinoSearchFormModel, sessionId: String) = {
    val formQueryModel = FormQueryModel(sessionId, formModel)
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
