package uk.gov.hmrc.homeofficesettledstatus.controllers

import scala.concurrent.Future

import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.homeofficesettledstatus.support.NonAuthPageISpec
import uk.gov.hmrc.homeofficesettledstatus.views.html.AccessibilityStatementPage

class AccessibilityStatementControllerISpec extends NonAuthPageISpec() {

  "AccessibilityStatementController" when {

    "GET /accessibility-statement" should {
      "display the accessibility-statement page" in {

        val controller = app.injector.instanceOf[AccessibilityStatementController]

        implicit val request = FakeRequest()
        val result: Future[Result] = controller.showPage(request)

        status(result) shouldBe OK

        val accessibilityStatementPage = app.injector.instanceOf[AccessibilityStatementPage]

        contentAsString(result) shouldBe accessibilityStatementPage().toString
      }
    }
  }
}
