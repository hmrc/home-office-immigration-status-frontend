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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed trait Search

final case class NinoSearch(
  nino: Nino,
  givenName: String,
  familyName: String,
  dateOfBirth: String,
  statusCheckRange: StatusCheckRange
) extends Search

object NinoSearch {
  private val ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def apply(
    nino: Nino,
    givenName: String,
    familyName: String,
    dateOfBirth: LocalDate,
    statusCheckRange: StatusCheckRange) =
    new NinoSearch(nino, givenName, familyName, dateOfBirth.format(ISO8601), statusCheckRange)

  implicit val formats: Format[NinoSearch] = Json.format[NinoSearch]
}

final case class MrzSearch(
  documentType: String,
  documentNumber: String,
  dateOfBirth: LocalDate,
  nationality: String,
  statusCheckRange: StatusCheckRange
) extends Search

object MrzSearch {
  implicit val formats: Format[MrzSearch] = Json.format[MrzSearch]
}