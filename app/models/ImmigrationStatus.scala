/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate

case class ImmigrationStatus(
  statusStartDate: LocalDate,
  statusEndDate: Option[LocalDate] = None,
  productType: String,
  immigrationStatus: String,
  noRecourseToPublicFunds: Boolean
) {

  def isEUS: Boolean = productType.take(3) == "EUS"

  private val hasExpired: Boolean = statusEndDate.exists(_.isBefore(LocalDate.now))
  val expiredMsg: String          = if (hasExpired) ".expired" else ""
}

object ImmigrationStatus {

  implicit val formats: Format[ImmigrationStatus] = Json.format[ImmigrationStatus]
}
