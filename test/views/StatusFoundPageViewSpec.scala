/*
 * Copyright 2024 HM Revenue & Customs
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

package views

import java.time.LocalDate
import models._
import org.jsoup.nodes._
import play.twirl.api.HtmlFormat
import utils.NinoGenerator.generateNino
import views.html.StatusFoundPage

class StatusFoundPageViewSpec extends ViewSpec {

  private val sut: StatusFoundPage = inject[StatusFoundPage]

  private def statusList(
    fillNumber: Int = 1,
    productType: String = "EUS",
    noRecourseToPublicFunds: Boolean = false
  ): List[ImmigrationStatus] =
    List.fill(fillNumber)(
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2021-01-01"),
        statusEndDate = Some(LocalDate.now().plusDays(1)),
        productType = productType,
        immigrationStatus = "ILR",
        noRecourseToPublicFunds = noRecourseToPublicFunds
      )
    )

  private def buildContext(
    statuses: List[ImmigrationStatus] = statusList(),
    nationality: String = "FRA"
  ): StatusFoundPageContext =
    StatusFoundPageContext(
      query = NinoSearchFormModel(
        nino = generateNino,
        givenName = "Pan",
        familyName = "Walker",
        dateOfBirth = LocalDate.parse("1980-12-10")
      ),
      result = StatusCheckResult(
        fullName = "Pan Walker",
        dateOfBirth = LocalDate.parse("1980-12-10"),
        nationality = nationality,
        statuses = statuses
      )
    )

  private def viewViaApply(context: StatusFoundPageContext = buildContext()): HtmlFormat.Appendable =
    sut.apply(context)(request, messages)
  private def viewViaRender(context: StatusFoundPageContext = buildContext()): HtmlFormat.Appendable =
    sut.render(context, request, messages)
  private def viewViaF(context: StatusFoundPageContext = buildContext()): HtmlFormat.Appendable =
    sut.f(context)(request, messages)

  private val idList: List[String] = List("immigrationRoute", "startDate", "expiryDate", "nino", "nationality", "dob")

  private val standardContentInput: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
    (".apply", viewViaApply(), viewViaApply(buildContext(statusList(2)))),
    (".render", viewViaRender(), viewViaRender(buildContext(statusList(2)))),
    (".f", viewViaF(), viewViaF(buildContext(statusList(2))))
  )

  private val warningContentInput: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
    (".apply", viewViaApply(buildContext(statusList(noRecourseToPublicFunds = true), "JPN")), viewViaApply()),
    (".render", viewViaRender(buildContext(statusList(noRecourseToPublicFunds = true), "JPN")), viewViaRender()),
    (".f", viewViaF(buildContext(statusList(noRecourseToPublicFunds = true), "JPN")), viewViaF())
  )

  private val immigrationRouteInput: Seq[(String, String)] = Seq(
    ("EUS", "EU Settlement Scheme"),
    ("STUDY", "Student"),
    ("DEPENDANT", "Dependants of a person with immigration permission"),
    ("WORK", "Worker"),
    ("FRONTIER_WORKER", "Frontier worker"),
    ("BNO", "British National Overseas"),
    ("BNO_LOTR", "British National Overseas (leave outside the rules)"),
    ("GRADUATE", "Graduate"),
    ("SPORTSPERSON", "International Sportsperson"),
    ("SETTLEMENT", "British National Overseas or Settlement Protection"),
    ("SETTLEMENT_LOTR", "British National Overseas with Settlement (leave outside the rules)"),
    ("TEMP_WORKER", "Temporary Worker"),
    ("EUS_EUN_JFM", "EU Settlement Scheme (joiner family member)"),
    ("EUS_FMFW", "EU Settlement Scheme (frontier worker family member)"),
    ("ARMED_FORCES", "Armed Forces Settlement"),
    ("error", "error")
  )

  private def standardContentTest(
    method: String,
    viewWithoutNoRecourseToPublicFunds: HtmlFormat.Appendable,
    viewWithPreviousStatuses: HtmlFormat.Appendable
  ): Unit =
    s"$method" must {
      val docWithoutNoRecourseToPublicFunds: Document = asDocument(viewWithoutNoRecourseToPublicFunds)
      val docWithPreviousStatuses: Document           = asDocument(viewWithPreviousStatuses)
      "have the title and heading" in {
        assertElementHasText(
          docWithoutNoRecourseToPublicFunds,
          "title",
          "Applicant has settled status - Check immigration status - GOV.UK"
        )
        assertElementHasText(docWithoutNoRecourseToPublicFunds, "#status-found-title", "Pan Walker has settled status")
      }

      "have all of the ids in the list in the correct order" in {
        idList.zipWithIndex.foreach { case (id, index) =>
          val row: Element = docWithoutNoRecourseToPublicFunds.select(".govuk-summary-list__row").get(index)
          row.select("dd").attr("id") mustBe id
        }
      }

      "not have the history section" when {
        "there is no previous status" in {
          assertNotRenderedById(docWithoutNoRecourseToPublicFunds, "previousStatuses")
        }
      }

      "have the history section" when {
        "there are previous statuses" in {
          assertRenderedById(docWithPreviousStatuses, "previousStatuses")
        }
      }

      "have the search again button" in {
        assertElementHasText(docWithoutNoRecourseToPublicFunds, "#search-again-button", "Search again")
        docWithoutNoRecourseToPublicFunds
          .getElementById("search-again-button")
          .attr("href") mustBe "/check-immigration-status"
      }
    }

  private def warningContentTest(
    method: String,
    viewWithWarnings: HtmlFormat.Appendable,
    viewWithoutWarnings: HtmlFormat.Appendable
  ): Unit =
    s"$method" must {
      val docWithWarnings: Document    = asDocument(viewWithWarnings)
      val docWithoutWarnings: Document = asDocument(viewWithoutWarnings)
      "have recourse warning message if noRecourseToPublicFunds is true" in {
        val message: String = "! Warning Child Benefit users only. This customer has no recourse to public funds, " +
          "but there may be exceptions. Eligibility needs to be checked on Home Office systems (such as ATLAS)."

        assertElementHasText(docWithWarnings, "#recourse-text", "No")
        assertElementHasText(docWithWarnings, "#recourse-warning", message)
      }

      "not have recourse warning message if noRecourseToPublicFunds is false" in {
        assertNotRenderedById(docWithoutWarnings, "#recourse-text")
        assertNotRenderedById(docWithoutWarnings, "recourse-warning")
      }

      "have zambrano warning message if context.isZambrano is true" in {
        val message: String = "! Warning This is a rest of the world national with an EU Settlement Scheme status. " +
          "Eligibility needs to be checked on Home Office systems (such as ATLAS)."

        assertElementHasText(docWithWarnings, "#zambrano-warning", message)
      }

      "not have zambrano warning message if context.isZambrano is false" in {
        assertNotRenderedById(docWithoutWarnings, "zambrano-warning")
      }
    }

  private def immigrationRouteTest(productType: String, expectedValue: String): Unit =
    ".apply" must {
      val doc: Document = asDocument(viewViaApply(buildContext(statusList(productType = productType), "D")))
      s"have the content $expectedValue for the immigration route immigration.${productType.toLowerCase}" in {
        assertElementHasText(doc, "#immigrationRoute", expectedValue)
      }
    }

  "StatusFoundPageView" when {
    standardContentInput.foreach(args => (standardContentTest _).tupled(args))
    warningContentInput.foreach(args => (warningContentTest _).tupled(args))
    immigrationRouteInput.foreach(args => (immigrationRouteTest _).tupled(args))
  }
}
