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

package views.components

import java.time.LocalDate

import models.ImmigrationStatus
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat
import views.ViewSpec
import views.html.components.PreviousStatuses

class PreviousStatusesSpec extends ViewSpec {

  private val sut: PreviousStatuses = inject[PreviousStatuses]

  private def viewViaApply(previousStatuses: Seq[ImmigrationStatus]): HtmlFormat.Appendable =
    sut.apply(previousStatuses)(messages)
  private def viewViaRender(previousStatuses: Seq[ImmigrationStatus]): HtmlFormat.Appendable =
    sut.render(previousStatuses, messages)
  private def viewViaF(previousStatuses: Seq[ImmigrationStatus]): HtmlFormat.Appendable =
    sut.f(previousStatuses)(messages)

  private def singleStatusList(
    productType: String = "EUS",
    immigrationStatus: String = "ILR",
    noRecourseToPublicFunds: Boolean = true
  ): Seq[ImmigrationStatus] =
    Seq(
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2012-01-01"),
        statusEndDate = Some(LocalDate.parse("2013-01-01")),
        productType = productType,
        immigrationStatus = immigrationStatus,
        noRecourseToPublicFunds = noRecourseToPublicFunds
      )
    )

  private val multipleStatusesList: Seq[ImmigrationStatus] = Seq(
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

  private val noStatusInput: Seq[(String, HtmlFormat.Appendable)] = Seq(
    (".apply", viewViaApply(Nil)),
    (".render", viewViaRender(Nil)),
    (".f", viewViaF(Nil))
  )

  private val singleStatusInput: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
    (".apply", viewViaApply(singleStatusList()), viewViaApply(singleStatusList(noRecourseToPublicFunds = false))),
    (".render", viewViaRender(singleStatusList()), viewViaRender(singleStatusList(noRecourseToPublicFunds = false))),
    (".f", viewViaF(singleStatusList()), viewViaF(singleStatusList(noRecourseToPublicFunds = false)))
  )

  private val multipleStatusesInput: Seq[(String, HtmlFormat.Appendable)] = Seq(
    (".apply", viewViaApply(multipleStatusesList)),
    (".render", viewViaRender(multipleStatusesList)),
    (".f", viewViaF(multipleStatusesList))
  )

  private val immigrationStatusInput: Seq[(String, String, String)] = Seq(
    ("EUS", "ILR", "EU Settlement Scheme - Settled status"),
    ("EUS", "LTR", "EU Settlement Scheme - Pre-settled status"),
    ("EUS", "COA_IN_TIME_GRANT", "EU Settlement Scheme - Pending EU Settlement Scheme application"),
    ("EUS", "POST_GRACE_PERIOD_COA_GRANT", "EU Settlement Scheme - Pending EU Settlement Scheme application"),
    ("STUDY", "LTE", "Student - Limited leave to enter"),
    ("STUDY", "LTR", "Student - Limited leave to remain"),
    ("DEPENDANT", "LTE", "Dependants of a person with immigration permission - Limited leave to enter"),
    ("DEPENDANT", "LTR", "Dependants of a person with immigration permission - Limited leave to remain"),
    ("DEPENDANT", "ILR", "Dependants of a person with immigration permission - Indefinite leave to remain"),
    ("WORK", "LTE", "Worker - Limited leave to enter"),
    ("WORK", "LTR", "Worker - Limited leave to remain"),
    ("BNO", "LTE", "British National Overseas - Limited leave to enter"),
    ("BNO", "LTR", "British National Overseas - Limited leave to remain"),
    ("BNO_LOTR", "LTE", "British National Overseas (leave outside the rules) - Limited leave to enter"),
    ("BNO_LOTR", "LTR", "British National Overseas (leave outside the rules) - Limited leave to remain"),
    ("SPORTSPERSON", "LTE", "International Sportsperson - Limited leave to enter"),
    ("SPORTSPERSON", "LTR", "International Sportsperson - Limited leave to remain"),
    ("TEMP_WORKER", "LTE", "Temporary Worker - Limited leave to enter"),
    ("TEMP_WORKER", "LTR", "Temporary Worker - Limited leave to remain"),
    ("GRADUATE", "LTR", "Graduate - Limited leave to remain"),
    ("FRONTIER_WORKER", "PERMIT", "Frontier worker - Frontier worker permit"),
    ("SETTLEMENT", "ILR", "British National Overseas or Settlement Protection - Indefinite leave to remain"),
    (
      "SETTLEMENT_LOTR",
      "ILR",
      "British National Overseas with Settlement (leave outside the rules) - Indefinite leave to remain"
    ),
    ("ARMED_FORCES", "ILR", "Armed Forces Settlement - Indefinite leave to remain"),
    ("ARMED_FORCES", "LTR", "Armed Forces Settlement - Limited leave to remain"),
    ("PROTECTION", "LTR", "Settlement Protection - Limited leave to remain"),
    ("PROTECTION_ROUTE", "LTR", "Settlement Protection - Limited leave to remain")
  )

  private def noStatusTest(method: String, view: HtmlFormat.Appendable): Unit =
    s"$method" must {
      val doc: Document           = asDocument(view)
      val emptyDocument: Document = Document.createShell("")
      "show nothing" when {
        "there are no previous statuses" in {
          doc.toString mustBe emptyDocument.toString
        }
      }
    }

  private def singleStatusTest(
    method: String,
    viewWithNoRecourseToPublicFunds: HtmlFormat.Appendable,
    viewWithoutNoRecourseToPublicFunds: HtmlFormat.Appendable
  ): Unit =
    s"$method" must {
      val docWithNoRecourseToPublicFunds: Document    = asDocument(viewWithNoRecourseToPublicFunds)
      val docWithoutNoRecourseToPublicFunds: Document = asDocument(viewWithoutNoRecourseToPublicFunds)
      "display a single status" when {
        "only one status is passed" in {
          assertRenderedById(docWithNoRecourseToPublicFunds, "history-0")
        }
      }

      "display all of the content in the list in the correct order" in {
        List(
          ("EU Settlement Scheme - Settled status", "status-found.previous.status", "status-previous-0"),
          ("01 January 2012", "status-found.previous.startDate", "startDate-previous-0"),
          ("01 January 2013", "status-found.previous.endDate", "expiryDate-previous-0"),
          ("No", "status-found.previous.recourse", "recourse-previous-0")
        ).zipWithIndex.foreach { case ((data, msgKey, id), index) =>
          val row: Elements =
            docWithNoRecourseToPublicFunds.select(s"#history-0 > .govuk-summary-list__row:nth-child(${index + 1})")
          assertCustomWidthRow(row, messages(msgKey), data, id, "third")
        }
      }

      "display noRecourseToPublicFunds field" when {
        "noRecourseToPublicFunds is true" in {
          assertElementHasText(docWithNoRecourseToPublicFunds, "#recourse-previous-0", "No")
        }
      }

      "not display noRecourseToPublicFunds field" when {
        "noRecourseToPublicFunds is false" in {
          assertNotRenderedById(docWithoutNoRecourseToPublicFunds, "recourse-previous-0")
        }
      }
    }

  private def multipleStatusesTest(method: String, view: HtmlFormat.Appendable): Unit =
    s"$method" must {
      val doc: Document = asDocument(view)
      "display multiple statuses" when {
        "multiple are passed" in {
          assertRenderedById(doc, "history-0")
          assertRenderedById(doc, "history-1")
          assertRenderedById(doc, "history-2")
        }
      }

      "not display the end date where it is not passed" in {
        assertNotRenderedById(doc, "expiryDate-previous-0")
      }

      "display noRecourseToPublicFunds field" when {
        "noRecourseToPublicFunds is true for different statuses" in {
          assertElementHasText(doc, "#recourse-previous-0", "No")
          assertElementHasText(doc, "#recourse-previous-2", "No")
          assertNotRenderedById(doc, "recourse-previous-1")
        }
      }
    }

  private def immigrationStatusTest(productType: String, immigrationStatus: String, expectedValue: String): Unit =
    ".apply" must {
      val doc: Document = asDocument(viewViaApply(singleStatusList(productType, immigrationStatus)))
      s"display the content $expectedValue for productType $productType and immigrationStatus $immigrationStatus" in {
        assertElementHasText(doc, "#status-previous-0", expectedValue)
      }
    }

  "PreviousStatuses" when {
    noStatusInput.foreach(args => noStatusTest.tupled(args))
    singleStatusInput.foreach(args => singleStatusTest.tupled(args))
    multipleStatusesInput.foreach(args => multipleStatusesTest.tupled(args))
    immigrationStatusInput.foreach(args => immigrationStatusTest.tupled(args))
  }
}
