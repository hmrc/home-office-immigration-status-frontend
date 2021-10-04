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

package uk.gov.hmrc.homeofficeimmigrationstatus.views

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.homeofficeimmigrationstatus.models.ImmigrationStatus._
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}

final case class StatusFoundPageContext(
  query: StatusCheckByNinoRequest,
  result: StatusCheckResult,
  searchAgainCall: Call) {

  val mostRecentStatus: Option[ImmigrationStatus] = result.mostRecentStatus
  val previousStatuses: Seq[ImmigrationStatus] = result.previousStatuses

  def currentStatusLabel(implicit messages: Messages): String = {
    val prefix = "status-found.current."
    mostRecentStatus match {
      case Some(status) =>
        (status.productType, status.immigrationStatus) match {
          case (EUS, LTR)                       => messages(prefix + "EUS.LTR" + status.expiredMsg)
          case (EUS, ILR)                       => messages(prefix + "EUS.ILR" + status.expiredMsg)
          case (pt, LTR) if pt != EUS           => messages(prefix + "nonEUS.LTR" + status.expiredMsg)
          case (pt, ILR) if pt != EUS           => messages(prefix + "nonEUS.ILR" + status.expiredMsg)
          case (pt, LTE) if pt != EUS           => messages(prefix + "nonEUS.LTE" + status.expiredMsg)
          case (EUS, COA_IN_TIME_GRANT)         => messages(prefix + "EUS.COA_IN_TIME_GRANT" + status.expiredMsg)
          case (EUS, POST_GRACE_PERIOD_COA)     => messages(prefix + "EUS.POST_GRACE_PERIOD_COA_GRANT" + status.expiredMsg)
          case (FRONTIER_WORKER, PERMIT)        => messages(prefix + "FRONTIER_WORKER.PERMIT" + status.expiredMsg)
          case (productType, immigrationStatus) => messages(prefix + "hasFBIS", productType, immigrationStatus)
        }
      case None => messages("status-found.current.noStatus")
    }
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
