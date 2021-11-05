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

package views.components

import models.ImmigrationStatus
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.ViewSpec
import views.html.components.PreviousStatuses

import java.time.LocalDate

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

  def singleStatusCustomImmigrationStatus(productType: String, immigrationStatus: String) =
    Seq(
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2012-01-01"),
        statusEndDate = Some(LocalDate.parse("2013-01-01")),
        productType = productType,
        immigrationStatus = immigrationStatus,
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
        ("EU Settlement Scheme - Indefinite leave to remain", "status-found.previous.status", "status-previous-0"),
        ("01 January 2012", "status-found.previous.startDate", "startDate-previous-0"),
        ("01 January 2013", "status-found.previous.endDate", "expiryDate-previous-0"),
        ("No", "status-found.previous.recourse", "recourse-previous-0")
      ).zipWithIndex.foreach {
        case ((data, msgKey, id), index) =>
          val row: Elements = doc.select(s"#history-0 > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRow(row, messages(msgKey), data, id)
      }
    }

    "display noRecourseToPublicFunds field" when {
      "when noRecourse is true" in {
        val doc: Document = asDocument(sut(singleStatus.map(_.copy(noRecourseToPublicFunds = true)))(messages))
        val e = doc.getElementById("recourse-previous-0")
        e.text() mustBe messages(s"status-found.previous.noRecourseToPublicFunds.true")
      }

      "each status is different" in {
        val doc = asDocument(sut(threeStatuses)(messages))
        doc.getElementById("recourse-previous-0").text() mustBe messages(
          "status-found.previous.noRecourseToPublicFunds.true")
        assertNotRenderedById(doc, "recourse-previous-1")
        doc.getElementById("recourse-previous-2").text() mustBe messages(
          "status-found.previous.noRecourseToPublicFunds.true")
      }
    }

    "Do not display noRecourseToPublicFunds field" when {
      "when noRecourse is false" in {
        val doc: Document = asDocument(sut(singleStatus.map(_.copy(noRecourseToPublicFunds = false)))(messages))
        assertNotRenderedById(doc, "recourse-previous-0")
      }
    }

    "not display the end date where it is not passed in" in {
      val doc: Document = asDocument(sut(threeStatuses)(messages))
      assertNotRenderedById(doc, "expiryDate-previous-0")
    }

    "ImmigrationStatus displays correct status" when {
      "EUS" when {
        "ILR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("EUS", "ILR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "EU Settlement Scheme - Indefinite leave to remain")
        }
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("EUS", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "EU Settlement Scheme - Leave to remain")
        }
      }

      "STUDY" when {
        "LTE" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("STUDY", "LTE"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Student - Leave to enter (FBIS)")
        }
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("STUDY", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Student - Leave to remain (FBIS)")
        }
      }

      "DEPENDANT" when {
        "LTE" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("DEPENDANT", "LTE"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(
            doc,
            "#status-previous-0",
            "Dependants of Skilled workers and Students - Leave to enter (FBIS)")
        }
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("DEPENDANT", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(
            doc,
            "#status-previous-0",
            "Dependants of Skilled workers and Students - Leave to remain (FBIS)")
        }
      }

      "WORK" when {
        "LTE" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("WORK", "LTE"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Worker - Leave to enter (FBIS)")
        }
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("WORK", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Worker - Leave to remain (FBIS)")
        }
      }

      "FRONTIER_WORKER" when {
        "PERMIT" in {
          val doc: Document =
            asDocument(sut(singleStatusCustomImmigrationStatus("FRONTIER_WORKER", "PERMIT"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Frontier worker - Permit (FBIS)")
        }
      }

      "BNO" when {
        "LTE" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("BNO", "LTE"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "British National Overseas - Leave to enter (FBIS)")
        }
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("BNO", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "British National Overseas - Leave to remain (FBIS)")
        }
      }

      "BNO_LOTR" when {
        "LTE" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("BNO_LOTR", "LTE"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "British National Overseas - Leave outside the rules (FBIS)")
        }
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("BNO_LOTR", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "British National Overseas - Leave outside the rules (FBIS)")
        }
      }

      "GRADUATE" when {
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("GRADUATE", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Graduate - Leave to remain (FBIS)")
        }
      }

      "EUS" when {
        "COA_IN_TIME_GRANT" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("EUS", "COA_IN_TIME_GRANT"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "EU Settlement - Certificate of application (In time)")
        }
        "POST_GRACE_PERIOD_COA_GRANT" in {
          val doc: Document =
            asDocument(sut(singleStatusCustomImmigrationStatus("EUS", "POST_GRACE_PERIOD_COA_GRANT"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "EU Settlement - Certificate of application (Out of time)")
        }
      }

      "SPORTSPERSON" when {
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("SPORTSPERSON", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "International Sportsperson - Leave to remain (FBIS)")
        }
        "LTE" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("SPORTSPERSON", "LTE"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "International Sportsperson - Leave to enter (FBIS)")
        }
      }

      "SETTLEMENT" when {
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("SETTLEMENT", "ILR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "British National Overseas - Settlement (FBIS)")
        }
      }

      "TEMP_WORKER" when {
        "LTR" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("TEMP_WORKER", "LTR"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Temporary Worker - Leave to remain (FBIS)")
        }
        "LTE" in {
          val doc: Document = asDocument(sut(singleStatusCustomImmigrationStatus("TEMP_WORKER", "LTE"))(messages))
          assertRenderedById(doc, "status-previous-0")
          assertElementHasText(doc, "#status-previous-0", "Temporary Worker - Leave to enter (FBIS)")
        }
      }
    }
  }
}
