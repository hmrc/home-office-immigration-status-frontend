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

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.homeofficesettledstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}

case class StatusFoundPageContext(
  statusCheckByNinoRequest: StatusCheckByNinoRequest,
  statusCheckResult: StatusCheckResult,
  searchAgainCall: Call) {

  val currentStatus: ImmigrationStatus = statusCheckResult.mostRecentStatus

  val hasStatus: Boolean = currentStatus.immigrationStatus != "NONE"

  val statusToken: String = if (hasStatus) "success" else "error"

  def statusLabel(implicit messages: Messages) = currentStatus match {
    case s if s.immigrationStatus == "LTR" => messages("app.hasPreSettledStatus")
    case s if s.immigrationStatus == "ILR" => messages("app.hasSettledStatus")
    case _                                 => messages("app.hasNoStatus")
  }

  def immigrationStatusLabel(status: String)(implicit messages: Messages): String = status match {
    case "LTR"  => messages("app.status.LTR")
    case "ILR"  => messages("app.status.ILR")
    case "ETLR" => messages("app.status.ETLR")
    case "NONE" => messages("app.status.NONE")
    case other  => other
  }

  def rightToPublicFundsMessage(implicit messages: Messages): String =
    if (currentStatus.rightToPublicFunds) messages("status-found.right-to-public-funds.true")
    else messages("status-found.right-to-public-funds.false")
}
