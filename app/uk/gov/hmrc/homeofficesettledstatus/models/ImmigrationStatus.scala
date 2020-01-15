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

case class ImmigrationStatus(
  // Underlying Immigration Status
  immigrationStatus: Option[String] = None,
  // 'Right to public funds status
  rightToPublicFunds: Option[Boolean] = None,
  // Expiry date of the 'right to public fund' Status in ISO 8601 format
  statusEndDate: Option[String] = None,
  // Start date of the 'right to public fund' Status in ISO 8601 format
  statusStartDate: Option[String] = None
)

object ImmigrationStatus {
  implicit val formats = Json.format[ImmigrationStatus]
}
