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

package uk.gov.hmrc.homeofficeimmigrationstatus.views.components

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import uk.gov.hmrc.homeofficeimmigrationstatus.views.ViewSpec
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.components.PreviousStatuses
import java.time.LocalDate
import uk.gov.hmrc.homeofficeimmigrationstatus.models.ImmigrationStatus

class PreviousStatusesComponentSpec extends ViewSpec {

  val sut: PreviousStatuses = inject[PreviousStatuses]

  val singleStatus = Seq(
    ImmigrationStatus(
      statusStartDate = LocalDate.parse("2012-01-01"),
      statusEndDate = Some(LocalDate.parse("2013-01-01")),
      productType = "EUS",
      immigrationStatus = "ILR",
      noRecourseToPublicFunds = true
    ))

  val threeStatuses = Seq(
    ImmigrationStatus(
      statusStartDate = LocalDate.parse("2013-01-01"),
      productType = "EUS",
      immigrationStatus = "ILR",
      noRecourseToPublicFunds = true
    ),
    ImmigrationStatus(
      statusStartDate = LocalDate.parse("2011-01-01"),
      statusEndDate = Some(LocalDate.parse("2012-01-01")),
      productType = "EUS",
      immigrationStatus = "LTR",
      noRecourseToPublicFunds = false
    ),
    ImmigrationStatus(
      statusStartDate = LocalDate.parse("2009-01-01"),
      statusEndDate = Some(LocalDate.parse("2010-01-01")),
      productType = "WORK",
      immigrationStatus = "LTR",
      noRecourseToPublicFunds = true
    )
  )

  "PreviousStatusesComponent" must {
    "show nothing" when {
      "no previous statuses" in {
        val doc: Document = asDocument(sut(Nil)(messages))
        val emptyDocument = Document.createShell("")

        doc.toString mustBe emptyDocument.toString
      }
    }

    "display a single status" when {
      "only one status is passed in" in {
        val doc: Document = asDocument(sut(singleStatus)(messages))
        assertRenderedById(doc, "history-0")
      }
    }

    "display multiple statuses" when {
      "multiple are passed in" in {
        val doc: Document = asDocument(sut(threeStatuses)(messages))
        assertRenderedById(doc, "history-0")
        assertRenderedById(doc, "history-1")
        assertRenderedById(doc, "history-2")
      }
    }

    "have all of the things in the list in the correct order" in {
      val doc: Document = asDocument(sut(singleStatus)(messages))
      List(
        ("Settled status", "status-found.previous.status", "status-previous-0"),
        ("placeholder", "status-found.previous.recourse", "recourse-previous-0"),
        ("01 January 2012", "status-found.previous.startDate", "startDate-previous-0"),
        ("01 January 2013", "status-found.previous.expiryDate", "expiryDate-previous-0")
      ).zipWithIndex.foreach {
        case ((data, msgKey, id), index) =>
          val row: Elements = doc.select(s"#history-0 > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRow(row, messages(msgKey), data, id)
      }
    }

    "not display the end date where it is not passed in" in {
      val doc: Document = asDocument(sut(threeStatuses)(messages))
      assertNotRenderedById(doc, "expiryDate-previous-0")
    }
  }
}
