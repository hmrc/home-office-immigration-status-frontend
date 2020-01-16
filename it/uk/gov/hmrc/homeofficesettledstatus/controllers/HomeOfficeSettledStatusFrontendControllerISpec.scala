package uk.gov.hmrc.homeofficesettledstatus.controllers

import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.homeofficesettledstatus.support.BaseISpec
import uk.gov.hmrc.http.HeaderCarrier

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

    "GET /" should {

      "display the start page" in {
        journeyState.set(Start, Nil)
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 200
        checkHtmlResultWithBodyText(result, htmlEscapedMessage("start.title"))
      }

      "redirect to the start page when elsewhere" in {
        journeyState.set(StatusCheckByNino, Nil)
        val result = controller.showStart(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/check-settled-status")
      }
    }

  }

}
