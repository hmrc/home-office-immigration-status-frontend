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

import play.api.i18n.Messages
import play.api.mvc.Call
import viewmodels.{RowViewModel => Row}
import models.{MrzSearchFormModel, NinoSearchFormModel, SearchFormModel}

case class StatusNotAvailablePageContext(query: SearchFormModel) {

  def fullName: Option[String] =
    query match {
      case q: NinoSearchFormModel => Some(s"${q.givenName} ${q.familyName}")
      case q: MrzSearchFormModel  => None
    }

  def notAvailablePersonalData(implicit messages: Messages) =
    query match {
      case q: NinoSearchFormModel =>
        Seq(
          Row("nino", "generic.nino", q.nino.nino),
          Row("givenName", "generic.givenName", q.givenName),
          Row("familyName", "generic.familyName", q.familyName),
          Row("dob", "generic.dob", DateFormat.format(messages.lang.locale)(q.dateOfBirth))
        )
      case q: MrzSearchFormModel =>
        Nil
    }
}
