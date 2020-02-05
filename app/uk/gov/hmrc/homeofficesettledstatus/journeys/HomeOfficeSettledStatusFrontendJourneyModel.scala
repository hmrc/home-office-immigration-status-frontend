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

package uk.gov.hmrc.homeofficesettledstatus.journeys

import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckError, StatusCheckResponse, StatusCheckResult}
import uk.gov.hmrc.play.fsm.JourneyModel

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HomeOfficeSettledStatusFrontendJourneyModel extends JourneyModel {

  sealed trait State
  sealed trait IsError

  override val root: State = State.Start

  object State {
    case object Start extends State

    case class StatusCheckByNino(maybeQuery: Option[StatusCheckByNinoRequest] = None) extends State

    case class StatusFound(
      correlationId: String,
      query: StatusCheckByNinoRequest,
      result: StatusCheckResult)
        extends State

    case class StatusCheckFailure(
      correlationId: String,
      query: StatusCheckByNinoRequest,
      error: StatusCheckError)
        extends State with IsError

    case class MultipleMatchesFound(correlationId: String, query: StatusCheckByNinoRequest)
        extends State with IsError
  }

  object Transitions {
    import State._

    def start(user: String) = Transition {
      case _ => goto(Start)
    }

    def showStatusCheckByNino(user: String) = Transition {
      case StatusCheckFailure(_, query, _) => goto(StatusCheckByNino(Some(query)))
      case _                               => goto(StatusCheckByNino())
    }

    def submitStatusCheckByNino(
      checkStatusByNino: StatusCheckByNinoRequest => Future[StatusCheckResponse])(user: String)(
      query: StatusCheckByNinoRequest) =
      Transition {
        case _: StatusCheckByNino =>
          checkStatusByNino(query).flatMap {

            case StatusCheckResponse(correlationId, Some(error), _) =>
              if (error.errCode.contains("ERR_CONFLICT")) {
                goto(MultipleMatchesFound(correlationId, query))
              } else {
                goto(StatusCheckFailure(correlationId, query, error))
              }

            case StatusCheckResponse(correlationId, _, Some(result)) =>
              goto(StatusFound(correlationId, query, result))
          }
      }
  }

}
