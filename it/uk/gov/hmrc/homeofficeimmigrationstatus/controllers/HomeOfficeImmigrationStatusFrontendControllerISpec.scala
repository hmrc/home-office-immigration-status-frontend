package uk.gov.hmrc.homeofficeimmigrationstatus.controllers

import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.journeys.HomeOfficeImmigrationStatusFrontendJourneyModel.State.{Start, StatusCheckByNino, StatusFound, StatusNotAvailable}
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckError, StatusCheckResult}
import uk.gov.hmrc.homeofficeimmigrationstatus.services.HomeOfficeImmigrationStatusFrontendJourneyServiceWithHeaderCarrier
import uk.gov.hmrc.homeofficeimmigrationstatus.stubs.{HomeOfficeImmigrationStatusStubs, JourneyTestData}
import uk.gov.hmrc.homeofficeimmigrationstatus.support.{AppISpec, InMemoryJourneyService, TestJourneyService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class HomeOfficeImmigrationStatusFrontendControllerISpec
    extends HomeOfficeImmigrationStatusFrontendControllerISpecSetup with HomeOfficeImmigrationStatusStubs
    with JourneyStateHelpers {

  import journey.model.State._

  "HomeOfficeImmigrationStatusFrontendController" when {

    "GET /" should {

      "redirect to the lookup page" in {
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-immigration-status/check-with-nino")
        journey.get shouldBe Some((Start, Nil))
      }

      "redirect to the clean lookup page when on status-check-failure" in {
        val existingQuery = StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")

        journey.set(
          StatusCheckFailure("123", existingQuery, StatusCheckError(errCode = "ERR_NOT_FOUND")),
          List(StatusCheckByNino(), Start))

        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-immigration-status/check-with-nino")
      }

      "redirect to the lookup page when elsewhere" in {
        journey.set(StatusCheckByNino(), Nil)
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-immigration-status/check-with-nino")
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
        journey.get shouldBe Some((StatusCheckByNino(), List()))
      }

      "display the lookup page filled with existing query parameters" in {
        val existingQuery = StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")

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

      "display the lookup page including the link to the Accessibility page" in {
        journey.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckByNino(fakeRequest)
        checkHtmlResultWithBodyText(result, s"""href="${routes.AccessibilityStatementController.showPage.url}"""")
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
        redirectLocation(result) shouldBe Some("/check-immigration-status/status-found")
        journey.get shouldBe Some(
          (StatusFound(correlationId, validQuery, expectedResultWithSingleStatus), List(StatusCheckByNino(), Start)))
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
        redirectLocation(result) shouldBe Some("/check-immigration-status/check-with-nino")
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
        redirectLocation(result) shouldBe Some("/check-immigration-status/status-check-failure")
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
        redirectLocation(result) shouldBe Some("/check-immigration-status/status-check-failure")
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
        redirectLocation(result) shouldBe Some("/check-immigration-status")
      }

      "redirect to start page if current state is StatusCheckFailure" in {
        val query = StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")
        val queryError = StatusCheckError(errCode = "ERR_NOT_FOUND")
        journey.set(StatusCheckFailure("sjdfhks123", query, queryError), List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusFound(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-immigration-status")
      }
    }

    "GET /status-not-available" should {

      "display status-not-available page if current state is StatusNotAvailable" in {
        val query = givenJourneyStateIsStatusNotAvailable
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusNotAvailable(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("status-not-available.title"))
        checkHtmlResultWithBodyText(result, query.nino.formatted)
        checkHtmlResultWithBodyText(result, s"${query.givenName} ${query.familyName}")
      }
    }

    "GET /status-check-failure" should {

      "display not found page" in {
        val query = StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")
        val queryError = StatusCheckError(errCode = "ERR_NOT_FOUND")
        journey.set(StatusCheckFailure("sjdfhks123", query, queryError), List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckFailure(fakeRequest)

        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("status-check-failure.title"))
        checkHtmlResultWithBodyText(result, query.nino.formatted)
        checkHtmlResultWithBodyText(result, query.givenName)
        checkHtmlResultWithBodyText(result, query.familyName)
        checkHtmlResultWithBodyText(result, "31 January 2001")
      }

      "display unique match not found page" in {
        val query = StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")
        val queryError = StatusCheckError(errCode = "ERR_CONFLICT")
        journey.set(StatusCheckFailure("sjdfhks123", query, queryError), List(StatusCheckByNino(), Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckFailure(fakeRequest)

        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("status-check-failure-conflict.title"))
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("status-check-failure-conflict.listParagraph"))
        checkHtmlResultWithBodyText(result, query.nino.formatted)
        checkHtmlResultWithBodyText(result, query.givenName)
        checkHtmlResultWithBodyText(result, query.familyName)
        checkHtmlResultWithBodyText(result, "31 January 2001")
      }

      "redirect to start page if current state is Start" in {
        journey.set(Start, Nil)
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckFailure(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-immigration-status")
      }

      "redirect to start page if current state is StatusFound" in {
        givenJourneyStateIsStatusFound
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckFailure(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-immigration-status")
      }
    }
  }
}

trait JourneyStateHelpers extends JourneyTestData {

  def journey: TestInMemoryHomeOfficeImmigrationStatusFrontendJourneyService

  def givenJourneyStateIsStatusFound(
    implicit headerCarrier: HeaderCarrier,
    timeout: Duration): (StatusCheckByNinoRequest, StatusCheckResult) = {

    journey
      .set(StatusFound(correlationId, validQuery, expectedResultWithMultipleStatuses), List(StatusCheckByNino(), Start))

    (validQuery, expectedResultWithMultipleStatuses)
  }

  def givenJourneyStateIsStatusNotAvailable(
    implicit headerCarrier: HeaderCarrier,
    timeout: Duration): StatusCheckByNinoRequest = {

    journey
      .set(StatusNotAvailable(correlationId, validQuery), List(StatusCheckByNino(), Start))

    validQuery
  }

}

class TestInMemoryHomeOfficeImmigrationStatusFrontendJourneyService
    extends HomeOfficeImmigrationStatusFrontendJourneyServiceWithHeaderCarrier with InMemoryJourneyService[HeaderCarrier]
    with TestJourneyService[HeaderCarrier]

trait HomeOfficeImmigrationStatusFrontendControllerISpecSetup extends AppISpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication: Application =
    appBuilder
      .overrides(
        bind(classOf[HomeOfficeImmigrationStatusFrontendJourneyServiceWithHeaderCarrier])
          .to(classOf[TestInMemoryHomeOfficeImmigrationStatusFrontendJourneyService]))
      .build()

  lazy val controller: HomeOfficeImmigrationStatusFrontendController =
    app.injector.instanceOf[HomeOfficeImmigrationStatusFrontendController]

  lazy val journey: TestInMemoryHomeOfficeImmigrationStatusFrontendJourneyService =
    controller.journeyService
      .asInstanceOf[TestInMemoryHomeOfficeImmigrationStatusFrontendJourneyService]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(controller.journeyService.journeyKey -> "fooId")

}
