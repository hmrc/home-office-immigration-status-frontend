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

import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.routes
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoFormModel, StatusCheckByNinoRequest}
import uk.gov.hmrc.homeofficeimmigrationstatus.views.{DateFormat, ViewSpec}
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.components.ShowChangeQuery

class ShowChangeQuerySpec extends ViewSpec {

  val sut = inject[ShowChangeQuery]

  //todo nino gen
  val query = StatusCheckByNinoFormModel(Nino("AB123456C"), "Pan", "", LocalDate.now().toString)

  val doc = asDocument(sut(query)(messages))

  "showChangeQuery" must {
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
            routes.StatusCheckByNinoController.onPageLoad.url
          )
      }
    }
  }

}
