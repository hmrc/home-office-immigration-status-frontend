package gov.uk.hmrc.homeofficesettledstatus.controllers

import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import gov.uk.hmrc.homeofficesettledstatus.support.BaseISpec

import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeSettledStatusFrontendControllerISpec extends BaseISpec {

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

    "GET /start" should {

      "redirect to start page with journeyId" in {
        journeyState.set(Start, Nil)
        val result = controller.showStart(FakeRequest())
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status")
      }

      "display start page" in {
        journeyState.set(Start, Nil)
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("start.title"))
      }
    }

    "POST /start" should {
      "redirect to end page" in {
        journeyState.set(Start, Nil)
        val result = controller.submitStart(
          fakeRequest.withFormUrlEncodedBody(
            "name"            -> "Henry",
            "postcode"        -> "",
            "telephoneNumber" -> "00000000001",
            "emailAddress"    -> "henry@example.com"))
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(
          routes.HomeOfficeSettledStatusFrontendController.showEnd().url)
      }
    }

    "GET /end" should {
      "display start page" in {
        journeyState.set(End("name", Some("postcode"), Some("telephone"), Some("email")), Nil)
        val result = controller.showEnd(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("end.title"))
      }
    }
  }

}
