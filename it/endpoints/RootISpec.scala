package endpoints

import play.api.http.Status.OK
import support.ISpec

class RootISpec extends ISpec {

  "GET /check-immigration-status/" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = request("/").get().futureValue

      result.status shouldBe OK
      result.body should include(htmlEscapedMessage("lookup.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }

}
