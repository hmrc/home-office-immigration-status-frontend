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

package uk.gov.hmrc.homeofficesettledstatus.models

import java.time.LocalDate

import play.api.libs.json.{Format, Json}

case class StatusCheckResult(
  // <name of the migrant that has matched
  fullName: String,
  // Date of birth of person being checked in ISO 8601 format
  dateOfBirth: LocalDate,
  // <the latest nationality that the matched migrant has provided to the Home Office
  // (ICAO 3 letter acronym - ISO 3166-1)
  nationality: String,
  statuses: List[ImmigrationStatus]
) {

  val mostRecentStatus: Option[ImmigrationStatus] =
    statuses
      .sortBy(f = _.statusStartDate.toEpochDay * -1)
      .headOption

  val previousStatuses: Seq[ImmigrationStatus] = {
    val sorted = statuses
      .sortBy(f = _.statusStartDate.toEpochDay * -1)
    if (sorted.isEmpty) Nil else sorted.tail
  }

}

object StatusCheckResult {
  implicit val formats: Format[StatusCheckResult] = Json.format[StatusCheckResult]
}
