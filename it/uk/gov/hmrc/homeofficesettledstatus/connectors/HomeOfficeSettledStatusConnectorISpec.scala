package uk.gov.hmrc.homeofficesettledstatus.connectors

import java.time.{LocalDate, ZoneId}

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckRange, StatusCheckResponse}
import uk.gov.hmrc.homeofficesettledstatus.stubs.HomeOfficeSettledStatusStubs
import uk.gov.hmrc.homeofficesettledstatus.support.BaseISpec
import uk.gov.hmrc.http._

import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeSettledStatusConnectorISpec extends BaseISpec with HomeOfficeSettledStatusStubs {
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val connector: HomeOfficeSettledStatusProxyConnector =
    app.injector.instanceOf[HomeOfficeSettledStatusProxyConnector]

  val request = StatusCheckByNinoRequest(
    "2001-01-31",
    "JANE",
    "DOE",
    Nino("RJ301829A"),
    Some(
      StatusCheckRange(
        Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(3)),
        Some(LocalDate.now(ZoneId.of("UTC")))))
  )

  "HomeOfficeSettledStatusProxyConnector" when {

    "POST /v1/status/public-funds/nino" should {

      "return status when range provided" in {
        givenStatusCheckResultWithRangeExample()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe defined
        result.error shouldBe None
      }

      "return status when no range provided" in {
        givenStatusCheckSucceeds()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe defined
        result.error shouldBe None
      }

      "return check error when 400 response ERR_REQUEST_INVALID" in {
        givenStatusCheckErrorWhenMissingInputField()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode shouldBe "ERR_REQUEST_INVALID"
      }

      "return check error when 404 response ERR_NOT_FOUND" in {
        givenStatusCheckErrorWhenStatusNotFound()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode shouldBe "ERR_NOT_FOUND"
      }

      "return check error when 400 response ERR_VALIDATION" in {
        givenStatusCheckErrorWhenDOBInvalid()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode shouldBe "ERR_VALIDATION"
      }

      "throw exception if other 4xx response" in {
        givenStatusPublicFundsByNinoStub(429, validRequestBodyWith3MonthsDateRange, "")

        an[Upstream4xxResponse] shouldBe thrownBy {
          await(connector.statusPublicFundsByNino(request))
        }
      }

      "throw exception if 5xx response" in {
        givenStatusPublicFundsByNinoStub(500, validRequestBodyWith3MonthsDateRange, "")

        an[Upstream5xxResponse] shouldBe thrownBy {
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
      HomeOfficeSettledStatusProxyConnector
        .extractResponseBody(errorMessage, "Response body: '") shouldBe responseBody
    }

    "return the json badRequestMessage if the prefix present" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.badRequestMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeSettledStatusProxyConnector
        .extractResponseBody(errorMessage, "Response body '") shouldBe responseBody
    }

    "return the whole message if prefix missing" in {
      val responseBody = """{"bar":"foo"}"""
      val errorMessage = errorGenerator.notFoundMessage("GET", "/test/foo/bar", responseBody)
      HomeOfficeSettledStatusProxyConnector
        .extractResponseBody(errorMessage, "::: '") shouldBe s"""{"error":{"errCode":"$errorMessage"}}"""
    }
  }

}
