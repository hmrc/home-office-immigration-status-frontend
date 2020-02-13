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
  query: StatusCheckByNinoRequest,
  result: StatusCheckResult,
  searchAgainCall: Call) {

  val EUS = "EUS"
  val LTR = "LTR"
  val ILR = "ILR"

  val settledStatusSet: Set[String] = Set(ILR, LTR)

  val mostRecentStatus: ImmigrationStatus = result.mostRecentStatus
    .getOrElse(
      throw new IllegalStateException("Expected user to have immigration status but got none"))

  val hasStatus: Boolean = mostRecentStatus.productType == EUS &&
    settledStatusSet.contains(mostRecentStatus.immigrationStatus)

  val statusClass: String = if (hasStatus) "success" else "error"

  def statusLabel(implicit messages: Messages) = mostRecentStatus match {
    case s if s.productType == EUS && s.immigrationStatus == LTR =>
      messages("app.hasPreSettledStatus")
    case s if s.productType == EUS && s.immigrationStatus == ILR =>
      messages("app.hasSettledStatus")
    case _ => messages("app.hasNoStatus")
  }

  def immigrationStatusLabel(status: String)(implicit messages: Messages): String = status match {
    case LTR   => messages("app.status.LTR")
    case ILR   => messages("app.status.ILR")
    case other => other
  }
}
