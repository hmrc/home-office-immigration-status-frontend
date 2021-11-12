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

package models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Nino
import java.time.{LocalDate, ZoneId}

case class StatusCheckByNinoFormModel(
  nino: Nino,
  givenName: String,
  familyName: String,
  dateOfBirth: LocalDate
) {
  def toRequest(timeRangeInMonths: Int): StatusCheckByNinoRequest = {
    val range = StatusCheckByNinoFormModel.statusCheckRange(timeRangeInMonths)
    StatusCheckByNinoRequest(nino, givenName, familyName, dateOfBirth, range)
  }
}

object StatusCheckByNinoFormModel {
  implicit val formats: OFormat[StatusCheckByNinoFormModel] = Json.format[StatusCheckByNinoFormModel]

  private def statusCheckRange(timeRangeInMonths: Int): StatusCheckRange = {
    val startDate = LocalDate.now(ZoneId.of("UTC")).minusMonths(timeRangeInMonths)
    val endDate = LocalDate.now(ZoneId.of("UTC"))
    StatusCheckRange(Some(startDate), Some(endDate))
  }
}
