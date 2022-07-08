package endpoints

import play.api.http.Status.SEE_OTHER
import play.api.libs.ws.WSResponse
import support.ISpec

class RootISpec extends ISpec {

  "GET /check-immigration-status/" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result: WSResponse = requestWithSession("/", "session-root").get().futureValue
      result.status shouldBe SEE_OTHER
      extractHeaderLocation(result) shouldBe Some(controllers.routes.SearchByNinoController.onPageLoad().url)
    }
  }
}
