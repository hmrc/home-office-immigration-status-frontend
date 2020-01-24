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

import play.api.mvc.Call
import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckResult}

case class StatusFoundPageContext(
  statusCheckByNinoRequest: StatusCheckByNinoRequest,
  statusCheckResult: StatusCheckResult,
  searchAgainCall: Call) {

  val hasStatus: Boolean = statusCheckResult.mostRecentStatus.immigrationStatus != "NONE"

  val statusToken: String = if (hasStatus) "success" else "error"

  val statusLabel: String => String = {
    case "LTR"  => "Leave To Remain"
    case "ILR"  => "Indefinite Leave To Remain"
    case "TLTR" => "Temporary Leave To Remain"
    case "NONE" => "None"
    case other  => other
  }
}
