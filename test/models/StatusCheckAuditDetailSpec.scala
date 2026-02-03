/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.mustEqual
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.OK
import play.api.libs.json.Json

import java.time.{LocalDate, ZoneId}

class StatusCheckAuditDetailSpec extends AnyWordSpecLike with Matchers {

  "writes" must {

    "write json" in {
      val search = MrzSearch(
        "documentType",
        "documentNumber",
        LocalDate.now,
        "nationality",
        StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
      )
      val statusCheckResult = StatusCheckResult("Damon Albarn", LocalDate.now, "GBR", Nil)
      val response          = StatusCheckSuccessfulResponse(Some("correlationId"), statusCheckResult)
      val result            = StatusCheckAuditDetail(OK, search, response)

      val resultJson = Json.toJson(result)

      (resultJson \ "statusCode").get mustEqual Json.toJson(OK)
      (resultJson \ "search").get mustEqual Json.toJson(search)
    }
  }
}
