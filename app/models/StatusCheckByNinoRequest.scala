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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class StatusCheckByNinoRequest(
  nino: Nino,
  givenName: String,
  familyName: String,
  dateOfBirth: String,
  statusCheckRange: StatusCheckRange
)

object StatusCheckByNinoRequest {
  private val ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def apply(
    nino: Nino,
    givenName: String,
    familyName: String,
    dateOfBirth: LocalDate,
    statusCheckRange: StatusCheckRange) =
    new StatusCheckByNinoRequest(nino, givenName, familyName, dateOfBirth.format(ISO8601), statusCheckRange)

  implicit val formats: OFormat[StatusCheckByNinoRequest] = Json.format[StatusCheckByNinoRequest]
}
