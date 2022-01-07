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
      result.body should include(htmlEscapedMessage("lookup.mrz.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }

  "POST /check-immigration-status/search-by-passport" should {
    "redirect to the result page" in {
      givenCheckByMrzSucceeds()
      givenAuthorisedForStride("TBC", "StrideUserId")

      val payload = Map(
        "dateOfBirth.year"  -> "2001",
        "dateOfBirth.month" -> "01",
        "dateOfBirth.day"   -> "31",
        "documentNumber"    -> "123456789",
        "documentType"      -> "PASSPORT",
        "nationality"       -> "AFG")

      val sessionId = "123"
      val result = request("/search-by-passport", sessionId).post(payload).futureValue

      result.status shouldBe OK
      result.body should include(htmlEscapedMessage("status-found.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }
}