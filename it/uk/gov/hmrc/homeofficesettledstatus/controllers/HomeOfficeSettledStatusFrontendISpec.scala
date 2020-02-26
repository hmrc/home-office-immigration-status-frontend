package uk.gov.hmrc.homeofficesettledstatus.controllers

import play.api.libs.ws.WSClient
import uk.gov.hmrc.homeofficesettledstatus.stubs.HomeOfficeSettledStatusStubs
import uk.gov.hmrc.homeofficesettledstatus.support.ServerISpec

class HomeOfficeSettledStatusFrontendISpec extends ServerISpec with HomeOfficeSettledStatusStubs {

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  "HomeOfficeSettledStatusFrontend" when {

    "GET /check-settled-status/" should {

      "show the lookup page" in {
        givenAuthorisedForStride("TBC", "StrideUserId")
        val result = await(wsClient.url(s"http://localhost:$port/check-settled-status").get())
        result.status shouldBe 200
        result.body should include(htmlEscapedMessage("lookup.title"))
      }
    }
  }

}
