package uk.gov.hmrc.homeofficeimmigrationstatus.controllers

import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, status}
import uk.gov.hmrc.homeofficeimmigrationstatus.support.NonAuthPageISpec
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.AccessibilityStatementPage

import scala.concurrent.Future

class AccessibilityStatementControllerISpec extends NonAuthPageISpec() {

  "AccessibilityStatementController" when {

    "GET /accessibility-statement" should {
      "display the accessibility-statement page" in {

        val controller = app.injector.instanceOf[AccessibilityStatementController]

        val result: Future[Result] = controller.showPage(request)

        status(result) shouldBe OK

        val accessibilityStatementPage = app.injector.instanceOf[AccessibilityStatementPage]

        contentAsString(result) shouldBe accessibilityStatementPage().toString
      }
    }
  }
}
