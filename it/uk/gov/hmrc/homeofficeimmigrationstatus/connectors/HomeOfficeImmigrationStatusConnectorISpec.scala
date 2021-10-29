package uk.gov.hmrc.homeofficeimmigrationstatus.connectors

import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}
import uk.gov.hmrc.homeofficeimmigrationstatus.stubs.HomeOfficeImmigrationStatusStubs
import uk.gov.hmrc.homeofficeimmigrationstatus.support.AppISpec
import uk.gov.hmrc.http._

import java.time.{LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeImmigrationStatusConnectorISpec extends HomeOfficeImmigrationStatusConnectorISpecSetup {

  "HomeOfficeImmigrationStatusProxyConnector" when {

    "statusPublicFundsByNino" should {

      "return status when range provided" in {
        givenStatusCheckResultWithRangeExample()

        val result: StatusCheckResponse = connector.statusPublicFundsByNino(request).futureValue

        result.result shouldBe defined
        result.error shouldBe None
      }

      "return status when no range provided" in {
        givenStatusCheckSucceeds()

        val result: StatusCheckResponse = connector.statusPublicFundsByNino(request).futureValue

        result.result shouldBe defined
        result.error shouldBe None
      }

      "return check error when 400 response ERR_REQUEST_INVALID" in {
        givenStatusCheckErrorWhenMissingInputField()

        val result: StatusCheckResponse = connector.statusPublicFundsByNino(request).futureValue

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode shouldBe "ERR_REQUEST_INVALID"
      }

      "return check error when 404 response ERR_NOT_FOUND" in {
        givenStatusCheckErrorWhenStatusNotFound()

        val result: StatusCheckResponse = connector.statusPublicFundsByNino(request).futureValue

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode shouldBe "ERR_NOT_FOUND"
      }

      "return check error when 400 response ERR_VALIDATION" in {
        givenStatusCheckErrorWhenDOBInvalid()

        val result: StatusCheckResponse = connector.statusPublicFundsByNino(request).futureValue

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode shouldBe "ERR_VALIDATION"
      }

      "throw exception if other 4xx response" in {
        givenStatusPublicFundsByNinoStub(429, validRequestBodyWithDateRange(), "")

        an[HomeOfficeImmigrationStatusProxyError] shouldBe thrownBy {
          await(connector.statusPublicFundsByNino(request))
        }
      }

      "throw exception if 5xx response" in {
        givenStatusPublicFundsByNinoStub(500, validRequestBodyWithDateRange(), "")

        an[HomeOfficeImmigrationStatusProxyError] shouldBe thrownBy {
          await(connector.statusPublicFundsByNino(request))
        }
      }
    }
  }

  val errorGenerator: HttpErrorFunctions = new HttpErrorFunctions {}

  "extractResponseBody" should {
    "return the json notFoundMessage if the prefix present" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.notFoundMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeImmigrationStatusProxyConnector
        .extractResponseBody(errorMessage, "Response body: '") shouldBe responseBody
    }

    "return the json badRequestMessage if the prefix present" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.badRequestMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeImmigrationStatusProxyConnector
        .extractResponseBody(errorMessage, "Response body '") shouldBe responseBody
    }

    "return the whole message if prefix missing" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.notFoundMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeImmigrationStatusProxyConnector
        .extractResponseBody(errorMessage, "::: '") shouldBe s"""{"error":{"errCode":"$errorMessage"}}"""
    }
  }
}

trait HomeOfficeImmigrationStatusConnectorISpecSetup extends AppISpec with HomeOfficeImmigrationStatusStubs with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication: Application = appBuilder.build()

  lazy val connector: HomeOfficeImmigrationStatusProxyConnector =
    app.injector.instanceOf[HomeOfficeImmigrationStatusProxyConnector]

  val request: StatusCheckByNinoRequest = StatusCheckByNinoRequest(
    Nino("RJ301829A"),
    "Doe",
    "Jane",
    "2001-01-31",
      StatusCheckRange(
        Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(queryMonths)),
        Some(LocalDate.now(ZoneId.of("UTC"))))
  )
}
