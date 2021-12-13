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

package views

import assets.constants.ImmigrationStatusConstant._
import models.{ImmigrationStatus, NinoSearchFormModel, StatusCheckResult}
import org.jsoup.nodes.{Document, Element}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.domain.Nino
import utils.NinoGenerator
import views.html.StatusFoundPage

import java.time.LocalDate

class StatusFoundPageViewSpec extends ViewSpec {

  val sut: StatusFoundPage = inject[StatusFoundPage]

  def buildContext(statuses: List[ImmigrationStatus] = List(ValidStatus)): StatusFoundPageContext =
    StatusFoundPageContext(
      NinoSearchFormModel(NinoGenerator.generateNino, "Pan", "", LocalDate.now()),
      StatusCheckResult("Pan", LocalDate.now(), "D", statuses)
    )

  "StatusFoundPageView" must {
    val context = buildContext()
    val doc: Document = asDocument(sut(context)(request, messages))

    "have a status found title" in {
      val e: Element = doc.getElementById("status-found-title")
      e.text() mustBe "Pan has settled status"
    }

    "have recourse to public funds field" when {
      "noRecourseToPublicFunds is true, and no warning is shown" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatus)))(request, messages)

        val doc = asDocument(html)

        assertNotRenderedById(doc, "#recourse-text")
        assertNotRenderedById(doc, "recourse-warning")
      }

      "noRecourseToPublicFunds is false, and the warning is shown" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusNoRecourceTrue)))(request, messages)

        val doc = asDocument(html)

        assertElementHasText(doc, "#recourse-text", messages("status-found.no"))
        assertElementHasText(doc, "#recourse-warning", "! Warning " + messages("status-found.warning"))
      }
    }

    "have all of the things in the list in the correct order" in {
      List(
        "immigrationRoute",
        "startDate",
        "expiryDate",
        "nino",
        "dob",
        "nationality"
      ).zipWithIndex.foreach {
        case (id, index) =>
          val row: Element = doc.select(s".govuk-summary-list__row").get(index)
          row.select("dd").attr("id") mustBe id
      }
    }

    "not have the history section" when {
      "there is not previous status" in {
        assertNotRenderedById(doc, "previousStatuses")
      }
    }

    "have the history section" when {
      val context = buildContext(statuses = List(ValidStatus, ValidStatus))
      val doc: Document = asDocument(sut(context)(request, messages))
      "there is previous statuses" in {
        assertRenderedById(doc, "previousStatuses")
      }
    }

    "have the search again button" in {
      val button = doc.select("#content > a")
      button.text() mustBe "Search again"
      button.attr("href") mustBe "/check-immigration-status"
      button.attr("id") mustBe "search-again-button"
    }

    "Immigration route" when {
      "EUS displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusNoRecourceTrue)))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "EU Settlement Scheme")
      }

      "STUDY displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("STUDY"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "Student")
      }

      "DEPENDANT displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("DEPENDANT"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "Dependants of Skilled workers and Students")
      }

      "WORK displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("WORK"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "Worker")
      }

      "FRONTIER_WORKER displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("FRONTIER_WORKER"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "Frontier worker")
      }

      "BNO displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("BNO"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "British National Overseas")
      }

      "BNO_LOTR displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("BNO_LOTR"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "British National Overseas (leave outside the rules)")
      }

      "GRADUATE displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("GRADUATE"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "Graduate")
      }

      "SPORTSPERSON displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("SPORTSPERSON"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "International Sportsperson")
      }

      "SETTLEMENT displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("SETTLEMENT"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "British National Overseas or Settlement Protection")
      }

      "TEMP_WORKER displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("TEMP_WORKER"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "Temporary Worker")
      }

      "EUS|EUN_JFM displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("EUS|EUN_JFM"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "EU Settlement Scheme (joiner family member)")
      }

      "EUS|_FMFW displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("EUS|_FMFW"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "EU Settlement Scheme (frontier worker family member)")
      }

      "Error with ProductType displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("error"))))(request, messages)

        val doc = asDocument(html)
        assertElementHasText(doc, "#immigrationRoute", "error")
      }
    }
  }

  "have zambrano warning message" when {
    "context.isZambrano is true" in {
      val context = StatusFoundPageContext(
        NinoSearchFormModel(NinoGenerator.generateNino, "Pan", "", LocalDate.now()),
        StatusCheckResult("Pan", LocalDate.now(), "JPN", List(ValidStatus))
      )
      val html: HtmlFormat.Appendable =
        sut(context)(request, messages)

      val doc = asDocument(html)

      assertElementHasText(doc, "#zambrano-warning", "! Warning " + messages("status-found.zambrano"))
    }
  }
  "have no zambrano warning message" when {
    "context.isZambrano is false" in {
      val context = StatusFoundPageContext(
        NinoSearchFormModel(NinoGenerator.generateNino, "Pan", "", LocalDate.now()),
        StatusCheckResult("Pan", LocalDate.now(), "FRA", List(ValidStatus))
      )

      val html: HtmlFormat.Appendable =
        sut(context)(request, messages)

      val doc = asDocument(html)

      assertNotRenderedById(doc, "zambrano-warning")
    }
  }
}
