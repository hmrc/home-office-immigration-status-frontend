/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficeimmigrationstatus.views

import org.joda.time.LocalDate
import org.jsoup.nodes.{Document, Element}
import play.api.mvc.Call
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.config.AppConfig
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckError}
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.MultipleMatchesFoundPage

class MultipleMatchesFoundViewSpec extends ViewSpec {

  val sut = inject[MultipleMatchesFoundPage]

  //todo nino gen
  val query = StatusCheckByNinoRequest(Nino("AB123456C"), "Pan", "", LocalDate.now().toString)

  val error: StatusCheckError = StatusCheckError("errorcode")

  //surely these arent dynamic and can just be got by reverse routes.
  val changeCall = Call("GET", "/change")
  val searchAgainCall = Call("GET", "search-again")

  "MultipleMatchesFoundPage" must {
    val doc: Document = asDocument(sut(query, error, changeCall, searchAgainCall)(request, messages))
    "have a status conflict title" in {
      val e: Element = doc.getElementById("status-check-failure-conflict-title")

      e.text() mustBe messages("status-check-failure-conflict.title")
    }

    "have paragraph text" in {
      doc.select("p .govuk-body").text() mustBe messages("status-check-failure-conflict.listParagraph")
    }
  }

}
