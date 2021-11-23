package connectors

import models.HomeOfficeError._
import models._
import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import stubs.HomeOfficeImmigrationStatusStubs
import support.BaseISpec
import uk.gov.hmrc.http._

import java.time.{LocalDate, ZoneId}
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeImmigrationStatusConnectorISpec extends HomeOfficeImmigrationStatusConnectorISpecSetup {

  "HomeOfficeImmigrationStatusProxyConnector" when {

    "statusPublicFundsByNino" should {

      "return status when range provided" in {
        givenCheckByNinoSucceeds()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('right)
      }

      "return status when no range provided" in {
        givenCheckByNinoSucceeds()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('right)
      }

      "return check error when 400 response ERR_REQUEST_INVALID" in {
        givenCheckByNinoErrorWhenMissingInputField()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe a[StatusCheckBadRequest]
      }

      "return check error when 404 response ERR_NOT_FOUND" in {
        givenStatusCheckErrorWhenStatusNotFound()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe a[StatusCheckNotFound]
      }

      "return check error when 400 response ERR_VALIDATION" in {
        givenStatusCheckErrorWhenDOBInvalid()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe a[StatusCheckBadRequest]
      }

      "throw exception if other 4xx response" in {
        givenStatusPublicFundsCheckStub("nino", 429, validByNinoRequestBody(), "")

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe a[OtherErrorResponse]
      }

      "throw exception if 5xx response" in {
        givenAnExternalServiceErrorCheckByNino

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe a[StatusCheckInternalServerError]
      }
    }
  }

}

trait HomeOfficeImmigrationStatusConnectorISpecSetup extends BaseISpec with HomeOfficeImmigrationStatusStubs with ScalaFutures {

  private val HEADER_X_CORRELATION_ID = "X-Correlation-Id"
  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders(HEADER_X_CORRELATION_ID -> UUID.randomUUID().toString)

  override def fakeApplication: Application = appBuilder.build()

  lazy val connector: HomeOfficeImmigrationStatusProxyConnector =
    app.injector.instanceOf[HomeOfficeImmigrationStatusProxyConnector]

  val request: NinoSearch = NinoSearch(
    nino,
    "Doe",
    "Jane",
    "2001-01-31",
      StatusCheckRange(
        Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(queryMonths)),
        Some(LocalDate.now(ZoneId.of("UTC"))))
  )
}
