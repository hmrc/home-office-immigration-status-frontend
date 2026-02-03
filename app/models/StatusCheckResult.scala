/*
 * Copyright 2026 HM Revenue & Customs
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

import java.time.LocalDate

import play.api.libs.json.{Format, JsNull, JsObject, Json, Reads, Writes}
import views.DateFormat
import java.util.Locale

case class StatusCheckResult(
  fullName: String,
  dateOfBirth: LocalDate,
  nationality: String, // (ICAO 3 letter acronym - ISO 3166-1)
  statuses: List[ImmigrationStatus]
) {
  def dobFormatted(locale: Locale): String        = DateFormat.format(locale)(dateOfBirth)
  private val statusesSortedByDate                = statuses.sortBy(f = _.statusStartDate.toEpochDay * -1)
  val mostRecentStatus: Option[ImmigrationStatus] = statusesSortedByDate.headOption
  val previousStatuses: Seq[ImmigrationStatus]    = statusesSortedByDate.drop(1)
}

object StatusCheckResult {
  val statusCheckResultReads: Reads[StatusCheckResult] = Json.reads[StatusCheckResult]
  val statusCheckResultWrites: Writes[StatusCheckResult] = Writes { models =>
    JsObject(
      Json
        .obj(
          "fullName"         -> models.fullName,
          "dateOfBirth"      -> models.dateOfBirth,
          "nationality"      -> models.nationality,
          "mostRecentStatus" -> models.mostRecentStatus,
          "previousStatuses" -> models.previousStatuses
        )
        .fields
        .filterNot(_._2 == JsNull)
    )
  }

  given statusCheckResultFormat: Format[StatusCheckResult] = Format(statusCheckResultReads, statusCheckResultWrites)
}
