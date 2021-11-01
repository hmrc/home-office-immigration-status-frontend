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
import models.ImmigrationStatus.EUS

case class ImmigrationStatus(
  // start date of this status
  statusStartDate: LocalDate,
  // end date of this status
  statusEndDate: Option[LocalDate] = None,
  // code representing the type of product that the status was associated with
  productType: String,
  // code representing the immigration status that is held
  immigrationStatus: String,
  // right to public funds status for this person
  noRecourseToPublicFunds: Boolean
) {

  def isEUS = productType == EUS

  private val hasExpired: Boolean = statusEndDate.exists(_.isBefore(LocalDate.now))
  val expiredMsg: String = if (hasExpired) ".expired" else ""
}

object ImmigrationStatus {

  val EUS = "EUS"

  implicit val formats: Format[ImmigrationStatus] = Json.format[ImmigrationStatus]
}
