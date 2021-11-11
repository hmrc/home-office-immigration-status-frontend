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
        result.left.get shouldBe StatusCheckBadRequest
      }

      "return check error when 404 response ERR_NOT_FOUND" in {
        givenStatusCheckErrorWhenStatusNotFound()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe StatusCheckNotFound
      }

      "return check error when 400 response ERR_VALIDATION" in {
        givenStatusCheckErrorWhenDOBInvalid()

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe StatusCheckBadRequest
      }

      "throw exception if other 4xx response" in {
        givenStatusPublicFundsByNinoStub(429, validRequestBodyWithDateRange(), "")

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe OtherErrorResponse(429)
      }

      "throw exception if 5xx response" in {
        givenStatusPublicFundsByNinoStub(500, validRequestBodyWithDateRange(), "")

        val result: Either[HomeOfficeError, StatusCheckResponse] = connector.statusPublicFundsByNino(request).futureValue

        result should be ('left)
        result.left.get shouldBe StatusCheckInternalServerError
      }
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
