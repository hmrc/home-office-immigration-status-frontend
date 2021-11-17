package controllers

import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{CookieHeaderEncoding, SessionCookieBaker}
import uk.gov.hmrc.domain.Nino
import models.{FormQueryModel, StatusCheckByNinoFormModel}
import stubs.{HomeOfficeImmigrationStatusStubs, JourneyTestData}
import support.ServerISpec
import repositories.SessionCacheRepository

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.{LocalDate, LocalDateTime}
import play.api.http.Status._

class HomeOfficeImmigrationStatusFrontendISpec
    extends HomeOfficeImmigrationStatusFrontendISpecSetup with HomeOfficeImmigrationStatusStubs with JourneyTestData {

  //todo seperate these per end point
  //todo behaves like shuttered endpoint

  "HomeOfficeImmigrationStatusFrontend" when {

    "GET /check-immigration-status/" should {
      "show the lookup page" in {
        givenAuthorisedForStride("TBC", "StrideUserId")

        val result = request("/").get().futureValue

        result.status shouldBe OK
        result.body should include(htmlEscapedMessage("lookup.title"))
        result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
      }
    }

    "GET /check-immigration-status/check-with-nino" should {
      "show the lookup page" in {
        givenAuthorisedForStride("TBC", "StrideUserId")

        val result = request("/check-with-nino").get().futureValue

        result.status shouldBe OK
        result.body should include(htmlEscapedMessage("lookup.title"))
        result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
      }
    }

    "POST /check-immigration-status/check-with-nino" should {
      "redirect to the result page" in {
        givenStatusCheckSucceeds()
        givenAuthorisedForStride("TBC", "StrideUserId")

        val payload = Map(
          "dateOfBirth.year"  -> "2001",
          "dateOfBirth.month" -> "01",
          "dateOfBirth.day"   -> "31",
          "familyName"        -> "Jane",
          "givenName"         -> "Doe",
          "nino"              -> "RJ301829A")

        val sessionId = "123"
        val result = request("/check-with-nino", sessionId).post(payload).futureValue

        result.status shouldBe OK
        result.body should include(htmlEscapedMessage("status-found.title"))
        result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
      }
    }

    "GET /check-immigration-status/status-result" should {
      "POST to the HO and show match found" in {
        givenStatusCheckSucceeds()
        givenAuthorisedForStride("TBC", "StrideUserId")

        val sessionId = "123"
        val query = StatusCheckByNinoFormModel(Nino("RJ301829A"), "Doe", "Jane", LocalDate.of(2001, 1, 31))
        setFormQuery(query, sessionId)

        val result = request("/status-result", sessionId).get().futureValue

        result.status shouldBe OK
        result.body should include(htmlEscapedMessage("status-found.title"))
        result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
      }

      "POST to the HO and show error page" in {
        givenAnExternalServiceError()
        givenAuthorisedForStride("TBC", "StrideUserId")
        
        val sessionId = "456"
        val query = StatusCheckByNinoFormModel(Nino("RJ301829A"), "Doe", "Jane", LocalDate.of(2001, 1, 31))
        setFormQuery(query, sessionId)

        val result = request("/status-result").get().futureValue

        result.status shouldBe INTERNAL_SERVER_ERROR
        result.body should include(htmlEscapedMessage("external.error.500.title"))
        result.body should include(htmlEscapedMessage("external.error.500.message"))
        result.body should include(htmlEscapedMessage("external.error.500.listParagraph"))
        result.body should include(htmlEscapedMessage("external.error.500.list-item1"))
        result.body should include(htmlEscapedMessage("external.error.500.list-item2"))
        result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
      }
    }

    "GET /check-immigration-status/foo" should {
      "return an error page not found" in {
        givenAuthorisedForStride("TBC", "StrideUserId")

        val result = request("/foo").get().futureValue

        result.status shouldBe NOT_FOUND
        result.body should include("This page can’t be found")
        result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
      }
    }
   }

}

trait HomeOfficeImmigrationStatusFrontendISpecSetup extends ServerISpec with ScalaFutures {

  override def fakeApplication: Application = appBuilder.build()

  lazy val sessionCookieBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  lazy val cookieHeaderEncoding: CookieHeaderEncoding = app.injector.instanceOf[CookieHeaderEncoding]

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  val baseUrl: String = s"http://localhost:$port/check-immigration-status"

  val cacheRepo = app.injector.instanceOf[SessionCacheRepository]

  def setFormQuery(formModel: StatusCheckByNinoFormModel, sessionId: String) = {
    val formQueryModel = FormQueryModel(sessionId, formModel)
    val selector = Json.obj("_id"  -> formQueryModel.id)
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
