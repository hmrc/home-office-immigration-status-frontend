package endpoints

import mocks.MockSessionCookie
import play.api.http.Status.{OK, SEE_OTHER}
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

class SearchByNinoISpec extends ISpec with HomeOfficeImmigrationStatusStubs with MockSessionCookie {

  "GET /check-immigration-status/search-by-nino" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = requestWithSession("/search-by-nino", "session-searchByNinoGet").get().futureValue

      result.status shouldBe OK
      result.body should include(htmlEscapedMessage("lookup.nino.title"))
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

      val result = requestWithSession("/search-by-nino", "nino-searchByPost").post(payload).futureValue

      result.status shouldBe SEE_OTHER
      extractHeaderLocation(result) shouldBe Some(controllers.routes.StatusResultController.onPageLoad.url)
    }
  }
}