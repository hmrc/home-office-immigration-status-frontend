/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.Json

case class StatusCheckResult(
  // Date of birth of person being checked in ISO 8601 format
  dateOfBirth: Option[String] = None,
  // Image of the person being checked
  facialImage: Option[String] = None,
  // Full name of person being checked
  fullName: Option[String] = None,
  // 'Right to public fund' statuses
  statuses: Option[List[ImmigrationStatus]] = None
)

object StatusCheckResult {
  implicit val formats = Json.format[StatusCheckResult]
}
