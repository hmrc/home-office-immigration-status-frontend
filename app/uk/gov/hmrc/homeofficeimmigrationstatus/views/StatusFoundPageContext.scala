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

case class StatusFoundPageContext(query: StatusCheckByNinoRequest, result: StatusCheckResult, searchAgainCall: Call) {

  val mostRecentStatus: Option[ImmigrationStatus] = result.mostRecentStatus
  val previousStatuses: Seq[ImmigrationStatus] = result.previousStatuses

  def currentStatusLabel(implicit messages: Messages): String = mostRecentStatus match {
    case Some(status) =>
      (status.productType, status.immigrationStatus) match {
        case (EUS, LTR)                       => messages("app.hasPreSettledStatus" + status.expiredMessages)
        case (EUS, ILR)                       => messages("app.hasSettledStatus" + status.expiredMessages)
        case (pt, LTR) if pt != EUS           => messages("app.nonEUS.LTR" + status.expiredMessages)
        case (pt, ILR) if pt != EUS           => messages("app.nonEUS.ILR" + status.expiredMessages)
        case (pt, LTE) if pt != EUS           => messages("app.nonEUS.LTE" + status.expiredMessages)
        case (EUS, COA_IN_TIME_GRANT)         => messages("app.EUS.COA_IN_TIME_GRANT" + status.expiredMessages)
        case (productType, immigrationStatus) => s" has FBIS status $productType - $immigrationStatus"
      }
    case None => messages("app.hasNoStatus")
  }

}

object StatusFoundPageContext {

  //todo what even is this?
  def immigrationStatusLabel(productType: String, status: String)(implicit messages: Messages): String =
    (productType, status) match {
      case (EUS, LTR) => messages("app.status.EUS_LTR")
      case (EUS, ILR) => messages("app.status.EUS_ILR")
      case _          => s"$productType + $status"
    }
}
