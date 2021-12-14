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

import controllers.routes
import models.{MrzSearchFormModel, NinoSearchFormModel}
import utils.NinoGenerator
import views.html.components.ShowChangeQuery
import views.{DateFormat, ViewSpec}
import java.time.LocalDate

class ShowChangeQuerySpec extends ViewSpec {

  val sut = inject[ShowChangeQuery]

  val nino = NinoGenerator.generateNino
  val query = NinoSearchFormModel(nino, "Pan", "", LocalDate.now())

  val dateOfBirth = LocalDate.now().minusYears(1)
  val mrzQuery = MrzSearchFormModel("documentType", "documentNumber", dateOfBirth, "nationality")

  val doc = asDocument(sut(query)(messages))

  "showChangeQuery" must {
    "nino - have all of the things in the list in the correct order" in {
      List(
        (query.nino.nino, "generic.nino", "nino", "nino", "generic.nino"),
        (query.givenName, "generic.givenName", "givenName", "givenName", "generic.givenName.lowercase"),
        (query.familyName, "generic.familyName", "familyName", "familyName", "generic.familyName.lowercase"),
        (
          DateFormat.format(messages.lang.locale)(query.dateOfBirth),
          "generic.dob",
          "dob",
          "dateOfBirth.day",
          "generic.dob.lowercase")
      ).zipWithIndex.foreach {
        case ((data, msgKey, id, fieldId, actionText), index) =>
          val row = doc.select(s"#inputted-data > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRowWithAction(
            row,
            messages(msgKey),
            data,
            id,
            s"${messages("generic.change")} ${messages(actionText)}",
            routes.SearchByNinoController.onPageLoad(false).url + "#" + fieldId
          )
      }
    }

    /*
    Row("idtype", "lookup.identity.label", q.documentType, "change-id-type", "documenttype", "mrz.idtype"),
    Row("idnumber", "lookup.mrz.label", q.documentNumber, "change-id-number", "documentNumber", "mrz.idnumber"),
    Row("nationality", "lookup.nationality.label", getCountryNameFor(q.nationality), "change-nationality", "nationality", "mrz.nationality"),
    Row("dob", "generic.dob", DateFormat.format(locale)(q.dateOfBirth), "change-dob", "dateOfBirth.day", "generic.dob.lowercase")
     */
    /*
    "mrz - have all of the things in the list in the correct order" in {
      List(
        (mrzQuery.documentType, "lookup.identity.label", "documenttype", "documenttype", "mrz.idtype", "", mrzQuery),
        (mrzQuery.documentNumber, "lookup.mrz.label", "documentNumber", "documentNumber", "mrz.idnumber", "", mrzQuery),
        (
          mrzQuery.nationality,
          "lookup.nationality.label",
          "nationality",
          "nationality",
          "mrz.nationality",
          "",
          mrzQuery),
        (
          DateFormat.format(messages.lang.locale)(mrzQuery.dateOfBirth),
          "generic.dob",
          "dob",
          "dateOfBirth.day",
          "generic.dob.lowercase",
          "",
          mrzQuery)
      ).zipWithIndex.foreach {
        case ((data, msgKey, id, fieldId, actionText, empty, model), index) =>
          val row = doc.select(s"#inputted-data > .govuk-summary-list__row:nth-child(${index + 1})")
          assertOneThirdRowWithAction(
            row,
            messages(msgKey),
            data,
            id,
            s"${messages("generic.change")} ${messages(actionText)}",
            routes.SearchByMrzController.onPageLoad(false).url + "#" + fieldId
          )
      }
    }*/
  }
}
