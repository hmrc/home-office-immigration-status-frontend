package uk.gov.hmrc.homeofficesettledstatus.controllers

import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}
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

      "display the lookup page" in {
        journeyState.set(StatusCheckByNino(), List(Start))
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = controller.showStatusCheckByNino(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("lookup.title"))
        journeyState.get shouldBe Some((StatusCheckByNino(), List(Start)))
      }
    }

    "POST /check-with-nino" should {

      "submit the lookup query and redirect to the confirmation page if request details pass validation" in {
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
          "2001-01-31",
          "string",
          "Jane Doe",
          List(ImmigrationStatus("ILR", true, Some("2018-12-12"), Some("2018-01-31"))))
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
  }

}
