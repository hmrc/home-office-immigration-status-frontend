package connectors

import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.domain.Nino
import models._
import stubs.HomeOfficeImmigrationStatusStubs
import support.AppISpec
import uk.gov.hmrc.http._
import models.HomeOfficeError._

import java.time.{LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID

class HomeOfficeImmigrationStatusConnectorISpec extends HomeOfficeImmigrationStatusConnectorISpecSetup {

  "HomeOfficeImmigrationStatusProxyConnector" when {

    "statusPublicFundsByNino" should {

      "return status when range provided" in {
        givenStatusCheckResultWithRangeExample()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('right)
      }

      "return status when no range provided" in {
        givenStatusCheckSucceeds()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('right)
      }

      "return check error when 400 response ERR_REQUEST_INVALID" in {
        givenStatusCheckErrorWhenMissingInputField()

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
        givenStatusPublicFundsByNinoStub(429, validRequestBodyWithDateRange(), "")

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe a[OtherErrorResponse]
      }

      "throw exception if 5xx response" in {
        givenStatusPublicFundsByNinoStub(500, validRequestBodyWithDateRange(), "")

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe a[StatusCheckInternalServerError]
      }
    }
  }

}

trait HomeOfficeImmigrationStatusConnectorISpecSetup extends AppISpec with HomeOfficeImmigrationStatusStubs with ScalaFutures {

  private val HEADER_X_CORRELATION_ID = "X-Correlation-Id"
  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders(HEADER_X_CORRELATION_ID -> UUID.randomUUID().toString)

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
