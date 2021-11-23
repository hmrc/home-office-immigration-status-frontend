package endpoints

import play.api.http.Status.OK
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

class SearchByNinoISpec extends ISpec with HomeOfficeImmigrationStatusStubs {

  "GET /check-immigration-status/search-by-nino" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = request("/search-by-nino").get().futureValue

      result.status shouldBe OK
      result.body should include(htmlEscapedMessage("lookup.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }

  "POST /check-immigration-status/search-by-nino" should {
    "redirect to the result page" in {
      givenCheckByNinoSucceeds()
      givenAuthorisedForStride("TBC", "StrideUserId")

      val payload = Map(
        "dateOfBirth.year"  -> "2001",
        "dateOfBirth.month" -> "01",
        "dateOfBirth.day"   -> "31",
        "familyName"        -> "Jane",
        "givenName"         -> "Doe",
        "nino"              -> nino.nino)

      val sessionId = "123"
      val result = request("/search-by-nino", sessionId).post(payload).futureValue

      result.status shouldBe OK
      result.body should include(htmlEscapedMessage("status-found.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }
}