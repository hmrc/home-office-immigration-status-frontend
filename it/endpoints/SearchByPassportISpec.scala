package endpoints

import mocks.MockSessionCookie
import play.api.http.Status.{OK, SEE_OTHER}
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

class SearchByPassportISpec extends ISpec with HomeOfficeImmigrationStatusStubs with MockSessionCookie {

  "GET /check-immigration-status/search-by-passport" should {
    "show the lookup page" in {
      givenAuthorisedForStride("TBC", "StrideUserId")

      val result = requestWithSession("/search-by-passport", "session-searchByPassportGet").get().futureValue

      result.status                                       shouldBe OK
      result.body                                           should include(htmlEscapedMessage("lookup.mrz.title"))
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
        "nationality"       -> "AFG"
      )

      val result = requestWithSession("/search-by-passport", "session-searchByPassportPost").post(payload).futureValue

      result.status                 shouldBe SEE_OTHER
      extractHeaderLocation(result) shouldBe Some(controllers.routes.StatusResultController.onPageLoad.url)
    }
  }
}
