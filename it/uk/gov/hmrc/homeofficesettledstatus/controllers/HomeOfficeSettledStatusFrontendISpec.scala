package uk.gov.hmrc.homeofficesettledstatus.controllers

import java.util.UUID

import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.libs.json.Format
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{CookieHeaderEncoding, Session, SessionCookieBaker}
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyStateFormats
import uk.gov.hmrc.homeofficesettledstatus.repository.CacheRepository
import uk.gov.hmrc.homeofficesettledstatus.services.{HomeOfficeSettledStatusFrontendJourneyService, MongoDBCachedJourneyService}
import uk.gov.hmrc.homeofficesettledstatus.stubs.{HomeOfficeSettledStatusStubs, JourneyTestData}
import uk.gov.hmrc.homeofficesettledstatus.support.{ServerISpec, TestJourneyService}

import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeSettledStatusFrontendISpec
    extends HomeOfficeSettledStatusFrontendISpecSetup with HomeOfficeSettledStatusStubs with JourneyTestData {

  import journey.model.State._

  "HomeOfficeSettledStatusFrontend" when {

    "GET /check-settled-status/" should {
      "show the lookup page" in {
        implicit val journeyId: JourneyId = JourneyId()
        givenAuthorisedForStride("TBC", "StrideUserId")

        val result = request("/").get().futureValue

        result.status shouldBe 200
        result.body should include(htmlEscapedMessage("lookup.title"))
        journey.getState shouldBe StatusCheckByNino()
      }
    }

    "POST /check-settled-status/check-with-nino" should {
      "submit the lookup form and show match found" in {
        implicit val journeyId: JourneyId = JourneyId()
        journey.setState(StatusCheckByNino())
        givenStatusCheckSucceeds()
        givenAuthorisedForStride("TBC", "StrideUserId")

        val payload = Map(
          "dateOfBirth.year"  -> "2001",
          "dateOfBirth.month" -> "01",
          "dateOfBirth.day"   -> "31",
          "familyName"        -> "Jane",
          "givenName"         -> "Doe",
          "nino"              -> "RJ301829A")

        val result = request("/check-with-nino").post(payload).futureValue

        result.status shouldBe 200
        result.body should include(htmlEscapedMessage("status-found.title"))
        journey.getState shouldBe StatusFound(correlationId, validQuery, expectedResultWithSingleStatus)
      }

      "submit the lookup form and show error page" in {
        implicit val journeyId: JourneyId = JourneyId()
        journey.setState(StatusCheckByNino())
        givenAnExternalServiceError()
        givenAuthorisedForStride("TBC", "StrideUserId")

        val payload = Map(
          "dateOfBirth.year"  -> "2001",
          "dateOfBirth.month" -> "01",
          "dateOfBirth.day"   -> "31",
          "familyName"        -> "Jane",
          "givenName"         -> "Doe",
          "nino"              -> "RJ301829A")

        val result = request("/check-with-nino").post(payload).futureValue

        result.status shouldBe 200
        result.body should include(htmlEscapedMessage("external.error.500.title"))
        result.body should include(htmlEscapedMessage("external.error.500.heading"))
        result.body should include(htmlEscapedMessage("external.error.500.message"))
        result.body should include(htmlEscapedMessage("external.error.500.listParagraph"))
        result.body should include(htmlEscapedMessage("external.error.500.list-item1"))
        result.body should include(htmlEscapedMessage("external.error.500.list-item2"))

        journey.getState shouldBe StatusCheckByNino()
      }
    }

    "GET /check-settled-status/foo" should {
      "return an error page not found" in {
        implicit val journeyId: JourneyId = JourneyId()
        givenAuthorisedForStride("TBC", "StrideUserId")

        val result = request("/foo").get().futureValue

        result.status shouldBe 404
        result.body should include("This page can’t be found")
        journey.get shouldBe None
      }
    }
  }

}

trait HomeOfficeSettledStatusFrontendISpecSetup extends ServerISpec with ScalaFutures {

  override def fakeApplication: Application = appBuilder.build()

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
  lazy val sessionCookieBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  lazy val cookieHeaderEncoding: CookieHeaderEncoding = app.injector.instanceOf[CookieHeaderEncoding]

  case class JourneyId(value: String = UUID.randomUUID().toString)

  // define test service capable of manipulating journey state
  lazy val journey: TestJourneyService[JourneyId] with HomeOfficeSettledStatusFrontendJourneyService[JourneyId] with MongoDBCachedJourneyService[JourneyId] = new TestJourneyService[JourneyId] with HomeOfficeSettledStatusFrontendJourneyService[JourneyId]
  with MongoDBCachedJourneyService[JourneyId] {

    override lazy val cacheRepository: CacheRepository = app.injector.instanceOf[CacheRepository]
    override lazy val applicationCrypto: ApplicationCrypto = app.injector.instanceOf[ApplicationCrypto]

    override val stateFormats: Format[model.State] =
      HomeOfficeSettledStatusFrontendJourneyStateFormats.formats

    override def getJourneyId(journeyId: JourneyId): Option[String] = Some(journeyId.value)
  }

  val baseUrl: String = s"http://localhost:$port/check-immigration-status"

  def request(path: String)(implicit journeyId: JourneyId): WSRequest =
    wsClient
      .url(s"$baseUrl$path")
      .withHttpHeaders(
        play.api.http.HeaderNames.COOKIE -> cookieHeaderEncoding.encodeCookieHeader(
          Seq(
            sessionCookieBaker.encodeAsCookie(Session(Map(journey.journeyKey -> journeyId.value)))
          )))

}
