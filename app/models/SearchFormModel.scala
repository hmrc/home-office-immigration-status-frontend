/*
 * Copyright 2024 HM Revenue & Customs
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

sealed trait SearchFormModel {
  def toSearch(timeRangeInMonths: Int): Search
}

object SearchFormModel {
  implicit val formats: OFormat[SearchFormModel] = Json.format[SearchFormModel]
}

final case class NinoSearchFormModel(
  nino: Nino,
  givenName: String,
  familyName: String,
  dateOfBirth: LocalDate
) extends SearchFormModel {
  def toSearch(timeRangeInMonths: Int): Search = {
    val range = NinoSearchFormModel.statusCheckRange(timeRangeInMonths)
    NinoSearch(nino, givenName, familyName, dateOfBirth, range)
  }
}

object NinoSearchFormModel {
  implicit val formats: OFormat[NinoSearchFormModel] = Json.format[NinoSearchFormModel]

  private def statusCheckRange(timeRangeInMonths: Int): StatusCheckRange = {
    val startDate = LocalDate.now(ZoneId.of("UTC")).minusMonths(timeRangeInMonths)
    val endDate   = LocalDate.now(ZoneId.of("UTC"))
    StatusCheckRange(Some(startDate), Some(endDate))
  }
}

final case class MrzSearchFormModel(
  documentType: String,
  documentNumber: String,
  dateOfBirth: LocalDate,
  nationality: String
) extends SearchFormModel {
  def toSearch(timeRangeInMonths: Int): Search = {
    val range = MrzSearchFormModel.statusCheckRange(timeRangeInMonths)
    MrzSearch(documentType, documentNumber, dateOfBirth, nationality, range)
  }
}

object MrzSearchFormModel {
  implicit val formats: OFormat[MrzSearchFormModel] = Json.format[MrzSearchFormModel]

  private def statusCheckRange(timeRangeInMonths: Int): StatusCheckRange = {
    val startDate = LocalDate.now(ZoneId.of("UTC")).minusMonths(timeRangeInMonths)
    val endDate   = LocalDate.now(ZoneId.of("UTC"))
    StatusCheckRange(Some(startDate), Some(endDate))
  }
}
