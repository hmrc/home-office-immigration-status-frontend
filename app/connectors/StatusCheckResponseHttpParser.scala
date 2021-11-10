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

package connectors

import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.http.Status._
import models._
import play.api.Logging
import scala.util.{Failure, Success, Try}
import models.HomeOfficeError._

object StatusCheckResponseHttpParser extends Logging {

  implicit object StatusCheckResponseReads extends HttpReads[Either[HomeOfficeError, StatusCheckResponse]] {

    override def read(
      method: String,
      url: String,
      response: HttpResponse): Either[HomeOfficeError, StatusCheckResponse] =
      response.status match {
        case OK =>
          Try(response.json.as[StatusCheckResponse]) match {
            case Success(res) =>
              logger.info(s"Successful request with response ${response.body}")
              Right(res)
            case Failure(e) =>
              logger.error(s"Invalid json returned in ${response.body}", e)
              Left(StatusCheckInvalidResponse)
          }
        case NOT_FOUND =>
          logger.info(s"Match not found with response ${response.body}")
          Left(StatusCheckNotFound)
        case BAD_REQUEST =>
          logger.error(s"Bad request returned with response ${response.body}")
          Left(StatusCheckBadRequest)
        case CONFLICT =>
          logger.warn(s"Multiple matches found for customer with response ${response.body}")
          Left(StatusCheckConflict)
        case INTERNAL_SERVER_ERROR =>
          logger.error(s"Internal server error returned with response ${response.body}")
          Left(StatusCheckInternalServerError)
        case status =>
          logger.error(s"An unhandled status was returned with response ${response.body}")
          Left(OtherErrorResponse)
      }
  }
}
