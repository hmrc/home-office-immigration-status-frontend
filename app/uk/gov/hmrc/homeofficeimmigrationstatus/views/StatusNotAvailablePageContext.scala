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

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.homeofficeimmigrationstatus.models.StatusCheckByNinoRequest
import uk.gov.hmrc.homeofficeimmigrationstatus.viewmodels.{RowViewModel => Row}

case class StatusNotAvailablePageContext(query: StatusCheckByNinoRequest, searchAgainCall: Call) {

  def fullName: String = s"${query.givenName} ${query.familyName}"

  def notAvailablePersonalData() =
    Seq(
      Some(Row("nino", "generic.nino", query.nino.formatted)),
      Some(Row("givenName", "generic.givenName", query.givenName)),
      Some(Row("familyName", "generic.familyName", query.familyName)),
      Some(Row("dob", "generic.dob", query.dateOfBirth))
    ).flatten
}
