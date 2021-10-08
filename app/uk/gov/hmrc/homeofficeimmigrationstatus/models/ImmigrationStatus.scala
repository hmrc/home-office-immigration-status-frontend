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

  //todo proper enum?
  val EUS = "EUS"
  val FRONTIER_WORKER = "FRONTIER_WORKER"

  val LTR = "LTR"
  val ILR = "ILR"
  val LTE = "LTE"
  val COA_IN_TIME_GRANT = "COA_IN_TIME_GRANT"
  val POST_GRACE_PERIOD_COA = "POST_GRACE_PERIOD_COA_GRANT"
  val PERMIT = "PERMIT"

  val settledStatusSet: Set[String] = Set(ILR, LTR)

  implicit val formats: Format[ImmigrationStatus] = Json.format[ImmigrationStatus]
}
