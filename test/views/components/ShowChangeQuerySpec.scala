/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.routes
import models._
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.NinoGenerator.generateNino
import views.html.components.ShowChangeQuery
import views._

class ShowChangeQuerySpec extends ViewSpec {

  private val sut: ShowChangeQuery = inject[ShowChangeQuery]

  private val dateOfBirth: LocalDate                   = LocalDate.now().minusYears(1)
  private val ninoSearchFormModel: NinoSearchFormModel = NinoSearchFormModel(generateNino, "Pan", "", LocalDate.now())
  private val mrzSearchFormModel: MrzSearchFormModel =
    MrzSearchFormModel("documentType", "documentNumber", dateOfBirth, "nationality")

  private def viewViaApply(query: SearchFormModel): HtmlFormat.Appendable  = sut.apply(query)(messages)
  private def viewViaRender(query: SearchFormModel): HtmlFormat.Appendable = sut.render(query, messages)
  private def viewViaF(query: SearchFormModel): HtmlFormat.Appendable      = sut.f(query)(messages)

  private val ninoInput: Seq[(String, HtmlFormat.Appendable)] = Seq(
    (".apply", viewViaApply(ninoSearchFormModel)),
    (".render", viewViaRender(ninoSearchFormModel)),
    (".f", viewViaF(ninoSearchFormModel))
  )

  private val mrzInput: Seq[(String, HtmlFormat.Appendable)] = Seq(
    (".apply", viewViaApply(mrzSearchFormModel)),
    (".render", viewViaRender(mrzSearchFormModel)),
    (".f", viewViaF(mrzSearchFormModel))
  )

  private def ninoTest(method: String, ninoSearchView: HtmlFormat.Appendable): Unit =
    s"$method for nino" must {
      val ninoDoc: Document = asDocument(ninoSearchView)
      "have all of the content in the list in the correct order" in {
        List(
          (ninoSearchFormModel.nino.nino, "generic.nino", "nino", "nino", "generic.nino"),
          (
            ninoSearchFormModel.givenName,
            "generic.givenName",
            "givenName",
            "givenName",
            "generic.givenName.lowercase"
          ),
          (
            ninoSearchFormModel.familyName,
            "generic.familyName",
            "familyName",
            "familyName",
            "generic.familyName.lowercase"
          ),
          (
            DateFormat.format(messages.lang.locale)(ninoSearchFormModel.dateOfBirth),
            "generic.dob",
            "dob",
            "dateOfBirth.day",
            "generic.dob.lowercase"
          )
        ).zipWithIndex.foreach { case ((data, msgKey, id, fieldId, actionText), index) =>
          val row = ninoDoc.select(s"#inputted-data > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRowWithAction(
            row,
            messages(msgKey),
            data,
            id,
            s"${messages("generic.change")} ${messages(actionText)}",
            routes.SearchByNinoController.onPageLoad().url + "#" + fieldId,
            "half"
          )
        }
      }
    }

  private def mrzTest(method: String, mrzSearchView: HtmlFormat.Appendable): Unit =
    s"$method for mrz" must {
      val mrzDoc: Document = asDocument(mrzSearchView)
      "have all of the content in the list in the correct order" in {
        List(
          (mrzSearchFormModel.documentType, "lookup.identity.label", "documentType", "documentType", "mrz.idtype"),
          (mrzSearchFormModel.documentNumber, "lookup.mrz.label", "documentNumber", "documentNumber", "mrz.idnumber"),
          (
            mrzSearchFormModel.nationality,
            "lookup.nationality.label",
            "nationality",
            "nationality",
            "mrz.nationality"
          ),
          (
            DateFormat.format(messages.lang.locale)(mrzSearchFormModel.dateOfBirth),
            "generic.dob",
            "dob",
            "dateOfBirth.day",
            "generic.dob.lowercase"
          )
        ).zipWithIndex.foreach { case ((data, msgKey, id, fieldId, actionText), index) =>
          val row = mrzDoc.select(s"#inputted-data > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRowWithAction(
            row,
            messages(msgKey),
            data,
            id,
            s"${messages("generic.change")} ${messages(actionText)}",
            routes.SearchByMrzController.onPageLoad().url + "#" + fieldId,
            "half"
          )
        }
      }
    }

  "ShowChangeQuery" when {
    ninoInput.foreach(args => (ninoTest _).tupled(args))
    mrzInput.foreach(args => (mrzTest _).tupled(args))
  }
}
