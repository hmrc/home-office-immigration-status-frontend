package uk.gov.hmrc.homeofficesettledstatus.controllers

import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State.{Start, StatusCheckByNino, StatusFound}
import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckError, StatusCheckResult}
import uk.gov.hmrc.homeofficesettledstatus.services.HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier
import uk.gov.hmrc.homeofficesettledstatus.stubs.{HomeOfficeSettledStatusStubs, JourneyTestData}
import uk.gov.hmrc.homeofficesettledstatus.support.{AppISpec, InMemoryJourneyService, TestJourneyService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class HomeOfficeSettledStatusFrontendControllerISpec
    extends HomeOfficeSettledStatusFrontendControllerISpecSetup with HomeOfficeSettledStatusStubs
    with JourneyStateHelpers {

  import journey.model.State._

  "HomeOfficeSettledStatusFrontendController" when {

    "GET /" should {

      "redirect to the lookup page" in {
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/check-with-nino")
        journey.get shouldBe Some((Start, Nil))
      }

      "redirect to the clean lookup page when on status-check-failure" in {
        val existingQuery = StatusCheckByNinoRequest(Nino("RJ301829A"), "DOE", "JANE", "2001-01-31")
        journey.set(
          StatusCheckFailure("123", existingQuery, StatusCheckError(errCode = "ERR_NOT_FOUND")),
          List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/check-with-nino")
      }

      "redirect to the lookup page when elsewhere" in {
        journey.set(StatusCheckByNino(), Nil)
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status/check-with-nino")
        journey.get shouldBe Some((Start, Nil))
      }
    }

    "GET /check-with-nino" should {

      "display the clean lookup page" in {
        journey.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckByNino(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("lookup.title"))
        journey.get shouldBe Some((StatusCheckByNino(), List(Start)))
      }

      "display the lookup page filled with existing query parameters" in {
        val existingQuery = StatusCheckByNinoRequest(Nino("RJ301829A"), "DOE", "JANE", "2001-01-31")
        journey.set(
          StatusCheckFailure("123", existingQuery, StatusCheckError(errCode = "ERR_NOT_FOUND")),
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
        journey.set(StatusCheckByNino(), List(Start))
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
        journey.get shouldBe Some(
          (
            StatusFound(correlationId, validQuery, expectedResultWithSingleStatus),
            List(StatusCheckByNino(), Start)))
      }

      "submit the lookup query and redisplay the form with errors if request details fails validation" in {
        journey.set(StatusCheckByNino(), List(Start))
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
        journey.set(StatusCheckByNino(), List(Start))
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
        journey.set(StatusCheckByNino(), List(Start))
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
        redirectLocation(result) shouldBe Some("/check-settled-status/status-check-failure")
      }

    }

    "GET /status-found" should {

      "display status found page if current state is StatusFound" in {
        val (query, expectedResult) = givenJourneyStateIsStatusFound
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusFound(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("status-found.title"))
        checkHtmlResultWithBodyText(result, query.nino.formatted)
        checkHtmlResultWithBodyText(result, expectedResult.fullName)
        checkHtmlResultWithBodyText(result, "31 January 2001")
      }

      "redirect to start page if current state is Start" in {
        journey.set(Start, Nil)
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusFound(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status")
      }

      "redirect to start page if current state is StatusCheckFailure" in {
        val query =
          StatusCheckByNinoRequest(Nino("RJ301829A"), "DOE", "JANE", "2001-01-31")
        val queryError = StatusCheckError(errCode = "ERR_NOT_FOUND")
        journey
          .set(
            StatusCheckFailure("sjdfhks123", query, queryError),
            List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusFound(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status")
      }
    }

    "GET /status-check-failure" should {

      "display not found page" in {
        val query =
          StatusCheckByNinoRequest(Nino("RJ301829A"), "DOE", "JANE", "2001-01-31")
        val queryError = StatusCheckError(errCode = "ERR_NOT_FOUND")
        journey
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

      "redirect to start page if current state is Start" in {
        journey.set(Start, Nil)
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckFailure(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status")
      }

      "redirect to start page if current state is StatusFound" in {
        givenJourneyStateIsStatusFound
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckFailure(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status")
      }
    }
  }

}

trait JourneyStateHelpers extends JourneyTestData {

  def journey: TestInMemoryHomeOfficeSettledStatusFrontendJourneyService

  def givenJourneyStateIsStatusFound(
    implicit headerCarrier: HeaderCarrier,
    timeout: Duration): (StatusCheckByNinoRequest, StatusCheckResult) = {

    journey
      .set(
        StatusFound(correlationId, validQuery, expectedResultWithMultipleStatuses),
        List(StatusCheckByNino(), Start))

    (validQuery, expectedResultWithMultipleStatuses)
  }

}

class TestInMemoryHomeOfficeSettledStatusFrontendJourneyService
    extends HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier
    with InMemoryJourneyService[HeaderCarrier] with TestJourneyService[HeaderCarrier]

trait HomeOfficeSettledStatusFrontendControllerISpecSetup extends AppISpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication: Application =
    appBuilder
      .overrides(
        bind(classOf[HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier])
          .to(classOf[TestInMemoryHomeOfficeSettledStatusFrontendJourneyService]))
      .build()

  lazy val controller: HomeOfficeSettledStatusFrontendController =
    app.injector.instanceOf[HomeOfficeSettledStatusFrontendController]

  lazy val journey: TestInMemoryHomeOfficeSettledStatusFrontendJourneyService =
    controller.journeyService
      .asInstanceOf[TestInMemoryHomeOfficeSettledStatusFrontendJourneyService]

  def fakeRequest = FakeRequest().withSession(controller.journeyService.journeyKey -> "fooId")

}
