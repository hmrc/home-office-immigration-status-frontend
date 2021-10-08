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

  def displayRecourseToPublicFunds: Boolean = mostRecentStatus.exists(_.noRecourseToPublicFunds)

  def currentStatusLabel(implicit messages: Messages): String = {
    val prefix = "status-found.current."
    mostRecentStatus match {
      case Some(status) =>
        def key(key: String): String = prefix + key + status.expiredMsg
        (status.productType, status.immigrationStatus) match {
          case (EUS, LTR)                       => messages(key("EUS.LTR"))
          case (EUS, ILR)                       => messages(key("EUS.ILR"))
          case (_, LTR)                         => messages(key("nonEUS.LTR"))
          case (_, ILR)                         => messages(key("nonEUS.ILR"))
          case (_, LTE)                         => messages(key("nonEUS.LTE"))
          case (EUS, COA_IN_TIME_GRANT)         => messages(prefix + "EUS.COA")
          case (EUS, POST_GRACE_PERIOD_COA)     => messages(prefix + "EUS.COA")
          case (FRONTIER_WORKER, PERMIT)        => messages(key("FRONTIER_WORKER.PERMIT"))
          case (productType, immigrationStatus) => messages(prefix + "hasFBIS", productType, immigrationStatus)
        }
      case None => messages(prefix + "noStatus")
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
