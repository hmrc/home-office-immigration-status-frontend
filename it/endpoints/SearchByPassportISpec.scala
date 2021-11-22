package endpoints

import play.api.http.Status.OK
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

class SearchByPassportISpec extends ISpec with HomeOfficeImmigrationStatusStubs {

  "GET /check-immigration-status/search-by-passport" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = request("/search-by-passport").get().futureValue

      result.status shouldBe OK
      result.body should include(htmlEscapedMessage("lookup.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }
}