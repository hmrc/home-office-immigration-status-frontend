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

package uk.gov.hmrc.homeofficesettledstatus.journeys

import play.api.libs.json._
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State._
import uk.gov.hmrc.play.fsm.JsonStateFormats

object HomeOfficeSettledStatusFrontendJourneyStateFormats extends JsonStateFormats[State] {

  val statusFound: OFormat[StatusFound] = Json.format[StatusFound]
  val statusNotAvailable: OFormat[StatusNotAvailable] = Json.format[StatusNotAvailable]
  val statusCheckByNino: OFormat[StatusCheckByNino] = Json.format[StatusCheckByNino]
  val statusCheckFailure: OFormat[StatusCheckFailure] = Json.format[StatusCheckFailure]

  override val serializeStateProperties: PartialFunction[State, JsValue] = {
    case s: StatusCheckByNino  => statusCheckByNino.writes(s)
    case s: StatusFound        => statusFound.writes(s)
    case s: StatusNotAvailable => statusNotAvailable.writes(s)
    case s: StatusCheckFailure => statusCheckFailure.writes(s)
  }

  override def deserializeState(stateName: String, properties: JsValue): JsResult[State] =
    stateName match {
      case "Start"              => JsSuccess(Start)
      case "StatusCheckByNino"  => statusCheckByNino.reads(properties)
      case "StatusFound"        => statusFound.reads(properties)
      case "StatusNotAvailable" => statusNotAvailable.reads(properties)
      case "StatusCheckFailure" => statusCheckFailure.reads(properties)
      case _                    => JsError(s"Unknown state name $stateName")
    }
}
