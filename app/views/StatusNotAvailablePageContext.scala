/*
 * Copyright 2025 HM Revenue & Customs
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

import config.Countries
import play.api.i18n.Messages
import viewmodels.{RowViewModel => Row}
import models.{MrzSearch, MrzSearchFormModel, NinoSearchFormModel, SearchFormModel, StatusCheckResult}

case class StatusNotAvailablePageContext(query: SearchFormModel, result: StatusCheckResult) {

  def notAvailablePersonalData(countries: Countries)(implicit messages: Messages): Seq[Row] =
    query match {
      case q: NinoSearchFormModel =>
        Seq(
          Row("nino", "generic.nino", q.nino.nino),
          Row("nationality", "generic.nationality", countries.getCountryNameFor(result.nationality)),
          Row("dob", "generic.dob", DateFormat.format(messages.lang.locale)(q.dateOfBirth))
        )
      case q: MrzSearchFormModel =>
        val documentTypeText = MrzSearch.documentTypeToMessageKey(q.documentType)
        Seq(
          Row("documentType", "lookup.identity.label", documentTypeText),
          Row("documentNumber", "lookup.mrz.label", q.documentNumber),
          Row("nationality", "generic.nationality", countries.getCountryNameFor(result.nationality)),
          Row("dob", "generic.dob", DateFormat.format(messages.lang.locale)(q.dateOfBirth))
        )
    }

}
