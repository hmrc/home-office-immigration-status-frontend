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

import java.time.{LocalDate, ZoneId}
import uk.gov.hmrc.homeofficesettledstatus.models.ImmigrationStatus.EUS
import uk.gov.hmrc.homeofficesettledstatus.models._
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

    case class StatusFound(correlationId: String, query: StatusCheckByNinoRequest, result: StatusCheckResult)
        extends State

    case class StatusNotAvailable(correlationId: String, query: StatusCheckByNinoRequest) extends State

    case class StatusCheckFailure(correlationId: String, query: StatusCheckByNinoRequest, error: StatusCheckError)
        extends State with IsError

  }

  object Transitions {
    import State._

    def start(user: String): Transition = Transition {
      case _ => goto(Start)
    }

    def showStatusCheckByNino(user: String): HomeOfficeSettledStatusFrontendJourneyModel.Transition = Transition {
      case StatusCheckFailure(_, query, _) => goto(StatusCheckByNino(Some(query)))
      case _                               => goto(StatusCheckByNino())
    }

    def submitStatusCheckByNino(
      checkStatusByNino: StatusCheckByNinoRequest => Future[StatusCheckResponse],
      defaultQueryTimeRangeInMonths: Int)(user: String)(
      query: StatusCheckByNinoRequest): HomeOfficeSettledStatusFrontendJourneyModel.Transition =
      Transition {
        case _: StatusCheckByNino =>
          val extendedQuery = {
            val startDate = query.statusCheckRange
              .flatMap(_.startDate)
              .getOrElse(LocalDate.now(ZoneId.of("UTC")).minusMonths(defaultQueryTimeRangeInMonths))
            val endDate =
              query.statusCheckRange.flatMap(_.endDate).getOrElse(LocalDate.now(ZoneId.of("UTC")))
            query.copy(statusCheckRange = Some(StatusCheckRange(Some(startDate), Some(endDate))))
          }

          checkStatusByNino(extendedQuery).flatMap {

            case StatusCheckResponse(correlationId, Some(error), _) =>
              goto(StatusCheckFailure(correlationId, query, error))

            case StatusCheckResponse(correlationId, _, Some(result)) =>
              if (result.statuses.isEmpty) goto(StatusNotAvailable(correlationId, query))
              else if (result.mostRecentStatus.exists(
                         s => s.productType == EUS && ImmigrationStatus.settledStatusSet.contains(s.immigrationStatus)))
                goto(StatusFound(correlationId, query, result))
              else
                goto(StatusCheckFailure(correlationId, query, StatusCheckError("UNSUPPORTED_STATUS")))

            case StatusCheckResponse(correlationId, _, _) =>
              goto(StatusNotAvailable(correlationId, query))
          }
      }
  }

}
