package uk.gov.hmrc.homeofficesettledstatus.controllers

import java.time.{LocalDate, LocalDateTime}

import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckError, StatusCheckResult}
import uk.gov.hmrc.homeofficesettledstatus.stubs.HomeOfficeSettledStatusStubs
import uk.gov.hmrc.homeofficesettledstatus.support.BaseISpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeSettledStatusFrontendControllerISpec
    extends BaseISpec with HomeOfficeSettledStatusStubs {

  private lazy val controller: HomeOfficeSettledStatusFrontendController =
    app.injector.instanceOf[HomeOfficeSettledStatusFrontendController]

  implicit val hc: HeaderCarrier = HeaderCarrier()
  override implicit lazy val app: Application = appBuilder
    .overrides(new TestAgentInvitationJourneyModule)
    .build()

  lazy val journeyState = app.injector.instanceOf[TestHomeOfficeSettledStatusFrontendJourneyService]

  import journeyState.model.State._

  def fakeRequest = FakeRequest().withSession(controller.journeyService.journeyKey -> "fooId")

  "HomeOfficeSettledStatusFrontendController" when {

    "GET /" should {

      "redirect to the lookup page" in {
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/check-with-nino")
        journeyState.get shouldBe Some((Start, Nil))
      }

      "redirect to the clean lookup page when on status-check-failure" in {
        val existingQuery = StatusCheckByNinoRequest("2001-01-31", "JANE", "DOE", Nino("RJ301829A"))
        journeyState.set(
          StatusCheckFailure(
            "123",
            existingQuery,
            StatusCheckError(errCode = Some("ERR_NOT_FOUND"))),
          List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/check-with-nino")
      }

      "redirect to the lookup page when elsewhere" in {
        journeyState.set(StatusCheckByNino(), Nil)
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/check-with-nino")
        journeyState.get shouldBe Some((Start, Nil))
      }
    }

    "GET /check-with-nino" should {

      "display the clean lookup page" in {
        journeyState.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckByNino(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("lookup.title"))
        journeyState.get shouldBe Some((StatusCheckByNino(), List(Start)))
      }

      "display the lookup page filled with existing query parameters" in {
        val existingQuery = StatusCheckByNinoRequest("2001-01-31", "JANE", "DOE", Nino("RJ301829A"))
        journeyState.set(
          StatusCheckFailure(
            "123",
            existingQuery,
            StatusCheckError(errCode = Some("ERR_NOT_FOUND"))),
          List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckByNino(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("lookup.title"))
        checkHtmlResultWithBodyText(result, s"""value="${existingQuery.nino.toString()}"""")
        checkHtmlResultWithBodyText(result, s"""value="${existingQuery.givenName}"""")
        checkHtmlResultWithBodyText(result, s"""value="${existingQuery.familyName}"""")
        checkHtmlResultWithBodyText(result, "value=\"2001\"")
        checkHtmlResultWithBodyText(result, "value=\"01\"")
        checkHtmlResultWithBodyText(result, "value=\"31\"")
      }
    }

    "POST /check-with-nino" should {

      "submit the lookup query and redirect to the status found if request details pass validation" in {
        journeyState.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        givenStatusCheckSucceeds()
        val request = fakeRequest
          .withFormUrlEncodedBody(
            "dateOfBirth.year"  -> "2001",
            "dateOfBirth.month" -> "01",
            "dateOfBirth.day"   -> "31",
            "familyName"        -> "Jane",
            "givenName"         -> "Doe",
            "nino"              -> "RJ301829A")
        val result = controller.submitStatusCheckByNino(request)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/status-found")
        val expectedQuery =
          StatusCheckByNinoRequest("2001-01-31", "JANE", "DOE", Nino("RJ301829A"))
        val expectedResult = StatusCheckResult(
          LocalDate.parse("2001-01-31"),
          "string",
          "Jane Doe",
          List(
            ImmigrationStatus(
              "ILR",
              true,
              Some(LocalDate.parse("2018-12-12")),
              Some(LocalDate.parse("2018-01-31"))))
        )
        journeyState.get shouldBe Some(
          (
            StatusFound("sjdfhks123", expectedQuery, expectedResult),
            List(StatusCheckByNino(), Start)))
      }

      "submit the lookup query and redisplay the form with errors if request details fails validation" in {
        journeyState.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val request = fakeRequest
          .withFormUrlEncodedBody(
            "dateOfBirth.year"  -> "2001",
            "dateOfBirth.month" -> "01",
            "dateOfBirth.day"   -> "31",
            "familyName"        -> "Jane",
            "givenName"         -> "Doe",
            "nino"              -> "invalid")
        val result = controller.submitStatusCheckByNino(request)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/check-with-nino")
      }

      "submit the lookup query and show status check failure" in {
        journeyState.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        givenStatusCheckErrorWhenStatusNotFound()
        val request = fakeRequest
          .withFormUrlEncodedBody(
            "dateOfBirth.year"  -> "2001",
            "dateOfBirth.month" -> "01",
            "dateOfBirth.day"   -> "31",
            "familyName"        -> "Jane",
            "givenName"         -> "Doe",
            "nino"              -> "RJ301829A")
        val result = controller.submitStatusCheckByNino(request)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/status-check-failure")
      }

      "submit the lookup query and show multiple matches found" in {
        journeyState.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        givenStatusCheckErrorWhenConflict()
        val request = fakeRequest
          .withFormUrlEncodedBody(
            "dateOfBirth.year"  -> "2001",
            "dateOfBirth.month" -> "01",
            "dateOfBirth.day"   -> "31",
            "familyName"        -> "Jane",
            "givenName"         -> "Doe",
            "nino"              -> "RJ301829A")
        val result = controller.submitStatusCheckByNino(request)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/multiple-matches-found")
      }

    }

    "GET /status-found" should {

      "display status found page" in {
        val query =
          StatusCheckByNinoRequest("2001-01-31", "JANE", "DOE", Nino("RJ301829A"))
        val queryResult = StatusCheckResult(
          LocalDate.parse("2001-01-31"),
          "string",
          "Jane Doe",
          List(
            ImmigrationStatus(
              "ILR",
              true,
              Some(LocalDate.parse("2018-12-12")),
              Some(LocalDate.parse("2018-01-31"))))
        )
        journeyState
          .set(StatusFound("sjdfhks123", query, queryResult), List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusFound(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("status-found.title"))
        checkHtmlResultWithBodyText(result, query.nino.formatted)
        checkHtmlResultWithBodyText(result, queryResult.fullName)
        checkHtmlResultWithBodyText(result, "31 January 2001")
      }
    }

    "GET /status-check-failure" should {

      "display not found page" in {
        val query =
          StatusCheckByNinoRequest("2001-01-31", "JANE", "DOE", Nino("RJ301829A"))
        val queryError = StatusCheckError(errCode = Some("ERR_NOT_FOUND"))
        journeyState
          .set(
            StatusCheckFailure("sjdfhks123", query, queryError),
            List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckFailure(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("status-check-failure.title"))
        checkHtmlResultWithBodyText(result, query.nino.formatted)
        checkHtmlResultWithBodyText(result, query.givenName)
        checkHtmlResultWithBodyText(result, query.familyName)
        checkHtmlResultWithBodyText(result, query.dateOfBirth)
      }
    }
  }

}
