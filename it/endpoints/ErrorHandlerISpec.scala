package endpoints

import play.api.http.Status.NOT_FOUND
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

class ErrorHandlerISpec extends ISpec with HomeOfficeImmigrationStatusStubs {

  "GET /check-immigration-status/foo" should {
    "return an error page not found" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = request("/foo").get().futureValue

      result.status                                       shouldBe NOT_FOUND
      result.body                                           should include("This page can’t be found")
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }

}
