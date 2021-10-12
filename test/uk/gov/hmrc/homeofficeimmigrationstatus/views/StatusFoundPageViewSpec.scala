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

import org.jsoup.nodes.{Document, Element}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.StatusFoundPage

import java.time.LocalDate
import org.jsoup.select.Elements
import assets.constants.ImmigrationStatusConstant.{ValidStatus, ValidStatusCustomProductType, ValidStatusNoRecourceFalse}

class StatusFoundPageViewSpec extends ViewSpec {

  val sut: StatusFoundPage = inject[StatusFoundPage]

  def buildContext(statuses: List[ImmigrationStatus] = List(ValidStatus)): StatusFoundPageContext =
    StatusFoundPageContext(
      //todo nino gen
      StatusCheckByNinoRequest(Nino("AB123456C"), "Pan", "", ""),
      StatusCheckResult("Pan", LocalDate.now(), "D", statuses),
      Call("", "/expected")
    )

  "StatusFoundPageView" must {
    val context = buildContext()
    val doc: Document = asDocument(sut(context)(request, messages))

    "have a status found title" in { //todo fixed in HOSS2-140
      val e: Element = doc.getElementById("status-found-title")
      e.text() mustBe "Pan has settled status"
    }

    "have recourse to public funds field" when {
      "noRecourseToPublicFunds is true" in {

        val html: HtmlFormat.Appendable = sut(buildContext(List(ValidStatus)))(request, messages)
        val doc = asDocument(html)

        assertRenderedById(doc, "recourse")
        assertElementHasText(doc, "#recourse-text", messages("status-found.no"))
        assertElementHasText(doc, "#recourse-warning", "! Warning " + messages("status-found.warning"))
      }
    }

    "not have recourse to public funds field" when {
      "noRecourseToPublicFunds is false" in {

        val html: HtmlFormat.Appendable = sut(buildContext(List(ValidStatusNoRecourceFalse)))(request, messages)
        val doc = asDocument(html)

        assertNotRenderedById(doc, "recourse")
        assertNotRenderedById(doc, "recourse-text")
        assertNotRenderedById(doc, "recourse-warning")
      }
    }

    "have all of the things in the list in the correct order" in {
      List(
        (context.query.nino.formatted, "generic.nino", "nino"),
        (context.result.dobFormatted(messages.lang.locale), "generic.dob", "dob"),
        (context.result.countryName.get, "generic.nationality", "nationality"),
        ( //todo move this to a view model. redic
          context.mostRecentStatus.map(a => DateFormat.format(messages.lang.locale)(a.statusStartDate)).get,
          "status-found.startDate",
          "startDate"),
        (
          context.mostRecentStatus.map(a => DateFormat.format(messages.lang.locale)(a.statusEndDate.get)).get,
          "status-found.expiryDate",
          "expiryDate"),
      ).zipWithIndex.foreach {
        case ((data, msgKey, id), index) =>
          val row: Elements = doc.select(s"#details > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRow(row, messages(msgKey), data, id)
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
        //this section is all subject to change and should be a separate view anyway, not testing in this pr.
      }
    }

    "have the search again button" in {
      val button = doc.select("#content > a")

      button.text() mustBe "Search again"
      button.attr("href") mustBe "/expected"
    }

    "Immigration route" when {
      "EUS displays" in {

        val html: HtmlFormat.Appendable = sut(buildContext(List(ValidStatusNoRecourceFalse)))(request, messages)
        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "EU Settlement Scheme")
      }

      "STUDY displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("STUDY"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "Student (FBIS)")
      }

      "DEPENDANT displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("DEPENDANT"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "Dependants of Skilled workers and Students (FBIS)")
      }

      "WORK displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("WORK"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "Worker (FBIS)")
      }

      "FRONTIER_WORKER displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("FRONTIER_WORKER"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "Frontier worker (FBIS)")
      }

      "BNO displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("BNO"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "British National Overseas (FBIS)")
      }

      "BNO_LOTR displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("BNO_LOTR"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "British National Overseas (FBIS)")
      }

      "GRADUATE displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("GRADUATE"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "Graduate (FBIS)")
      }

      "SPORTSPERSON displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("SPORTSPERSON"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "International Sportsperson (FBIS)")
      }

      "SETTLEMENT displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("SETTLEMENT"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "British National Overseas (FBIS)")
      }

      "TEMP_WORKER displays" in {
        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("TEMP_WORKER"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "Temporary Worker (FBIS)")
      }

      "Error with ProductType displays" in {

        val html: HtmlFormat.Appendable =
          sut(buildContext(List(ValidStatusCustomProductType("error"))))(request, messages)

        val doc = asDocument(html)

        assertRenderedById(doc, "route")
        assertElementHasText(doc, "#immigrationRoute", "error")
      }
    }
  }
}
