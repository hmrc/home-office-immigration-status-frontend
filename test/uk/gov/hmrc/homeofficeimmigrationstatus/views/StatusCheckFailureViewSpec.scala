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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.routes
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckError}
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.StatusCheckFailurePage

class StatusCheckFailureViewSpec extends ViewSpec {

  val sut: StatusCheckFailurePage = inject[StatusCheckFailurePage]

  //todo nino gen
  val query = StatusCheckByNinoRequest(Nino("AB123456C"), "Pan", "", LocalDate.now().toString)
  val doc: Document = asDocument(
    sut(
      query,
      StatusCheckError("err"),
      routes.HomeOfficeImmigrationStatusFrontendController.showStatusCheckByNino,
      routes.HomeOfficeImmigrationStatusFrontendController.showStart
    )(request, messages))

  "StatusCheckFailurePage" must {
    "have a status conflict title" in {
      val e: Element = doc.getElementById("status-check-failure-title")

      e.text() mustBe messages("status-check-failure.title")
    }

    "have paragraph text" in {
      doc.select(".govuk-body").text() mustBe messages("status-check-failure.listParagraph")
    }

    "have the bullet points" when {
      List(1, 2).foreach { n =>
        s"is bullet point number $n" in {
          doc.getElementById(s"item$n").text() mustBe messages(s"status-check-failure.list-item$n")
        }
      }
    }

    "have personal details heading" in {
      val e: Element = doc.getElementById("personal-details")

      e.text() mustBe messages("status-check-failure.heading2CustomerDetails")
    }

    "have all of the things in the list in the correct order" in {
      List(
        (query.nino.formatted, "generic.nino", "nino", "generic.nino"),
        (query.givenName, "generic.givenName", "givenName", "generic.givenName.lowercase"),
        (query.familyName, "generic.familyName", "familyName", "generic.familyName.lowercase"),
        (
          DateFormat.formatDatePattern(messages.lang.locale)(query.dateOfBirth),
          "generic.dob",
          "dob",
          "generic.dob.lowercase")
      ).zipWithIndex.foreach {
        case ((data, msgKey, id, actionText), index) =>
          val row = doc.select(s"#inputted-data > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRowWithAction(
            row,
            messages(msgKey),
            data,
            id,
            s"${messages("generic.change")} ${messages(actionText)}",
            routes.HomeOfficeImmigrationStatusFrontendController.showStatusCheckByNino.url
          )
      }
    }

    "have the search again button" in {
      val button = doc.select("#content > a")

      button.text() mustBe "Search again"
      button.attr("href") mustBe routes.HomeOfficeImmigrationStatusFrontendController.showStart.url
    }
  }

}
