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

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckResult}
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.StatusFoundPage
import java.time.LocalDate

import assets.constants.ImmigrationStatusConstant.{ValidStatusNoResourceFalse, ValidStatusNoResourceTrue}

class StatusFoundPageViewSpec extends ViewSpec {

  val sut: StatusFoundPage = inject[StatusFoundPage]
  implicit val messages: Messages = inject[MessagesApi].preferred(Seq.empty[Lang])

  val context = StatusFoundPageContext(
    StatusCheckByNinoRequest(Nino("AB123456C"), "Pan", "", ""),
    StatusCheckResult("Pan", LocalDate.now(), "", Nil),
    Call("", "/")
  )

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val html: HtmlFormat.Appendable = sut(context)(request, messages)

  // todo we should make a ViewSpec trait with method for this kind of stuff
  val doc: Document = Jsoup.parse(html.toString())

  "StatusFoundPageViewSpec" must {
    "status found title must exist in test suit" in {
      val e: Element = doc.getElementById("status-found-title")
      e.text() mustBe "Panhas no immigration status"
    }

    "when noRecourseToPublicFunds is true, recourse is set to No and the warning and the field is shown" in {

      val context = StatusFoundPageContext(
        StatusCheckByNinoRequest(Nino("AB123456C"), "Pan", "", ""),
        StatusCheckResult("Pan", LocalDate.now(), "", List(ValidStatusNoResourceTrue)),
        Call("", "/")
      )

      val html: HtmlFormat.Appendable = sut(context)(request, messages)
      val doc = asDocument(html)

      assertRenderedById(doc, "recourse")
      assertRenderedById(doc, "recourse-text")
      assertRenderedById(doc, "recourse-warning")
      assertElementHasText(doc, "#recourse-text", "No")
    }

    "when noRecourseToPublicFunds is false, recourse is set to Yes and the warning and the field are hidden" in {

      val context = StatusFoundPageContext(
        StatusCheckByNinoRequest(Nino("AB123456C"), "Pan", "", ""),
        StatusCheckResult("Pan", LocalDate.now(), "", List(ValidStatusNoResourceFalse)),
        Call("", "/")
      )

      val html: HtmlFormat.Appendable = sut(context)(request, messages)
      val doc = asDocument(html)

      assertNotRenderedById(doc, "recourse-text")
      assertNotRenderedById(doc, "recourse-warning")
    }
  }
}
