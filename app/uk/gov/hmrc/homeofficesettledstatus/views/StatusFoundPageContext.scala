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

package uk.gov.hmrc.homeofficesettledstatus.views

import java.time.LocalDate

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.homeofficesettledstatus.models.ImmigrationStatus.{EUS, ILR, LTR}
import uk.gov.hmrc.homeofficesettledstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}

case class StatusFoundPageContext(query: StatusCheckByNinoRequest, result: StatusCheckResult, searchAgainCall: Call) {

  val mostRecentStatus: Option[ImmigrationStatus] = result.mostRecentStatus
  val previousStatuses: Seq[ImmigrationStatus] = result.previousStatuses

  val hasSettledStatus: Boolean = mostRecentStatus.map(_.productType).contains(EUS) &&
    mostRecentStatus
      .map(_.immigrationStatus)
      .exists(ImmigrationStatus.settledStatusSet.contains)

  def today: LocalDate = LocalDate.now()

  val hasExpiredSettledStatus: Boolean = hasSettledStatus && mostRecentStatus.exists(_.hasExpired)

  val statusClass: String = if (hasSettledStatus) "success" else "error"

  def currentStatusLabel(implicit messages: Messages) = mostRecentStatus match {
    case Some(s) if s.productType == EUS && s.immigrationStatus == LTR =>
      if (s.hasExpired) messages("app.hasPreSettledStatus.expired")
      else s" ${messages("app.hasPreSettledStatus")}"

    case Some(s) if s.productType == EUS && s.immigrationStatus == ILR =>
      if (s.hasExpired) messages("app.hasSettledStatus.expired")
      else s" ${messages("app.hasSettledStatus")}"

    case _ => messages("app.hasNoStatus")
  }

}

object StatusFoundPageContext {

  def immigrationStatusLabel(productType: String, status: String)(implicit messages: Messages): String =
    (productType, status) match {
      case (EUS, LTR) => messages("app.status.EUS_LTR")
      case (EUS, ILR) => messages("app.status.EUS_ILR")
      case _          => s"$productType + $status"
    }
}
