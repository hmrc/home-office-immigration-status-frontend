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

package uk.gov.hmrc.homeofficeimmigrationstatus.models

import java.time.LocalDate
import play.api.libs.json.{Format, Json}

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
  private val hasExpired: Boolean = statusEndDate.exists(_.isBefore(LocalDate.now))
  val expiredMsg: String = if (hasExpired) ".expired" else ""
}

object ImmigrationStatus {

  val EU = "EU"
  val STUDY = "STUDY"
  val DEPENDANT = "DEPENDANT"
  val WORK = "WORK"
  val FRONTIER_WORKER = "FRONTIER_WORKER"
  val BNO = "BNO"
  val BNO_LOTR = "BNO_LOTR"
  val GRADUATE = "GRADUATE"
  val EUS = "EUS"
  val SPORTSPERSON = "SPORTSPERSON"
  val SETTLEMENT = "SETTLEMENT"
  val TEMP_WORKER = "TEMP_WORKER"

  val ILR = "ILR"
  val LTR = "LTR"
  val LTE = "LTE"
  val PERMIT = "PERMIT"
  val COA_IN_TIME_GRANT = "COA_IN_TIME_GRANT"
  val POST_GRACE_PERIOD_COA = "POST_GRACE_PERIOD_COA_GRANT"

  implicit val formats: Format[ImmigrationStatus] = Json.format[ImmigrationStatus]
}
