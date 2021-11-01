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

import java.time.LocalDate
import play.api.libs.json.{Format, Json}
import views.{DateFormat, ISO31661Alpha3}

import java.util.Locale

case class StatusCheckResult(
  fullName: String,
  dateOfBirth: LocalDate,
  nationality: String, // (ICAO 3 letter acronym - ISO 3166-1)
  statuses: List[ImmigrationStatus]
) {
  //todo seperate these to view model?
  val countryName: String = ISO31661Alpha3.getCountryNameFor(nationality)
  def dobFormatted(locale: Locale): String = DateFormat.format(locale)(dateOfBirth)
  private val statusesSortedByDate = statuses.sortBy(f = _.statusStartDate.toEpochDay * -1)
  val mostRecentStatus: Option[ImmigrationStatus] = statusesSortedByDate.headOption
  val previousStatuses: Seq[ImmigrationStatus] = statusesSortedByDate.drop(1)
}

object StatusCheckResult {
  implicit val formats: Format[StatusCheckResult] = Json.format[StatusCheckResult]
}
