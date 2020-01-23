package uk.gov.hmrc.homeofficesettledstatus.connectors

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

  val request = StatusCheckByNinoRequest("2001-01-31", "JANE", "DOE", Nino("RJ301829A"))

  "HomeOfficeSettledStatusProxyConnector" when {

    "POST /v1/status/public-funds/nino" should {

      "return status when range provided" in {
        givenStatusCheckResultWithRangeExample()
        val request2 =
          request.copy(
            statusCheckRange = Some(StatusCheckRange(Some("2019-07-15"), Some("2019-04-15"))))

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request2))

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

      "return check error when 400 response" in {
        givenStatusCheckErrorWhenMissingInputField()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode.get shouldBe "ERR_REQUEST_INVALID"
      }

      "return check error when 404 response" in {
        givenStatusCheckErrorWhenStatusNotFound()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode.get shouldBe "ERR_NOT_FOUND"
      }

      "return check error when 422 response" in {
        givenStatusCheckErrorWhenDOBInvalid()

        val result: StatusCheckResponse =
          await(connector.statusPublicFundsByNino(request))

        result.result shouldBe None
        result.error shouldBe defined
        result.error.get.errCode.get shouldBe "ERR_VALIDATION"
      }

      "throw exception if other 4xx response" in {
        givenStatusPublicFundsByNinoStub(401, validRequestBody, "")

        an[Upstream4xxResponse] shouldBe thrownBy {
          await(connector.statusPublicFundsByNino(request))
        }
      }

      "throw exception if 5xx response" in {
        givenStatusPublicFundsByNinoStub(500, validRequestBody, "")

        an[Upstream5xxResponse] shouldBe thrownBy {
          await(connector.statusPublicFundsByNino(request))
        }
      }
    }
  }

}
