package endpoints

import models.NinoSearchFormModel
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import stubs.HomeOfficeImmigrationStatusStubs
import support.ISpec

import java.time.LocalDate

class StatusResultISpec extends ISpec with HomeOfficeImmigrationStatusStubs {

  "GET /check-immigration-status/status-result" should {
    "POST to the HO and show match found" in {
      givenCheckByNinoSucceeds()
      givenAuthorisedForStride("TBC", "StrideUserId")

      val sessionId = "123"
      val query = NinoSearchFormModel(nino, "Doe", "Jane", LocalDate.of(2001, 1, 31))
      setFormQuery(query, sessionId)

      val result = request("/status-result", sessionId).get().futureValue

      result.status shouldBe OK
      result.body should include(htmlEscapedMessage("status-found.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }

    "POST to the HO and show error page" in {
      givenAnExternalServiceErrorCheckByNino()
      givenAuthorisedForStride("TBC", "StrideUserId")

      val sessionId = "456"
      val query = NinoSearchFormModel(nino, "Doe", "Jane", LocalDate.of(2001, 1, 31))
      setFormQuery(query, sessionId)

      val result = request("/status-result").get().futureValue

      result.status shouldBe INTERNAL_SERVER_ERROR
      result.body should include(htmlEscapedMessage("external.error.500.title"))
      result.body should include(htmlEscapedMessage("external.error.500.message"))
      result.body should include(htmlEscapedMessage("external.error.500.listParagraph"))
      result.body should include(htmlEscapedMessage("external.error.500.list-item1"))
      result.body should include(htmlEscapedMessage("external.error.500.list-item2"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }
}
