/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

      val sessionId = "session-statusResultGet"
      val query     = NinoSearchFormModel(nino, "Doe", "Jane", LocalDate.of(2001, 1, 31))
      setFormQuery(query, sessionId)

      val result = requestWithSession("/status-result", sessionId).get().futureValue

      result.status                                       shouldBe OK
      result.body                                           should include(htmlEscapedMessage("status-found.title"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }

    "POST to the HO and show error page" in {
      givenAnExternalServiceErrorCheckByNino()
      givenAuthorisedForStride("TBC", "StrideUserId")

      val sessionId = "session-statusResultErrorPage"
      val query     = NinoSearchFormModel(nino, "Doe", "Jane", LocalDate.of(2001, 1, 31))
      setFormQuery(query, sessionId)

      val result = requestWithSession("/status-result", sessionId).get().futureValue

      result.status                                       shouldBe INTERNAL_SERVER_ERROR
      result.body                                           should include(htmlEscapedMessage("external.error.500.title"))
      result.body                                           should include(htmlEscapedMessage("external.error.500.message"))
      result.body                                           should include(htmlEscapedMessage("external.error.500.helpdesk-link"))
      result.body                                           should include(htmlEscapedMessage("external.error.500.helpdesk-text"))
      result.headers.get("Cache-Control").map(_.mkString) shouldBe Some("no-cache, no-store, must-revalidate")
    }
  }
}
