package controllers

import controllers.actions.AuthAction
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results._
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import play.api.test.{FakeRequest, Injecting}
import support.BaseISpec
import uk.gov.hmrc.http.SessionKeys

class AuthActionsISpec extends AuthActionISpecSetup {

  "withAuthorisedWithStrideGroup" should {

    "call body with a valid authProviderId" in {

      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = TestController.test()(request)

      status(result)          shouldBe 200
      contentAsString(result) shouldBe "Passed Auth"
      verifyAuthoriseAttempt()
    }

    "redirect to log in page when user not authenticated" in {
      givenRequestIsNotAuthorised("SessionRecordNotFound")

      val result = TestController.test()(request)
      status(result)             shouldBe 303
      redirectLocation(result).get should include("/stride/sign-in")
      verifyAuthoriseAttempt()
    }

    "redirect to log in page when user authenticated with different provider" in {
      givenRequestIsNotAuthorised("UnsupportedAuthProvider")

      val result = TestController.test()(request)
      status(result)             shouldBe 303
      redirectLocation(result).get should include("/stride/sign-in")
      verifyAuthoriseAttempt()
    }
  }
}

trait AuthActionISpecSetup extends BaseISpec with Injecting {

  override def fakeApplication: Application = appBuilder.build()

  implicit val request: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")

  object TestController {
    val sut = inject[AuthAction]

    def test() = sut(Ok("Passed Auth"))
  }
}
