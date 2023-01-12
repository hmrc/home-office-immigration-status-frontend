/*
 * Copyright 2023 HM Revenue & Customs
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

object StatusCheckResponseHttpParser extends Logging {

  implicit object StatusCheckResponseReads extends HttpReads[StatusCheckResponseWithStatus] {

    private val UNKNOWN_ERROR = "UNKNOWN_ERROR"

    override def read(method: String, url: String, response: HttpResponse): StatusCheckResponseWithStatus = {
      val correlationId: Option[String] = response.header(Constants.HEADER_X_CORRELATION_ID)
      response.status match {
        case OK =>
          Try(response.json.as[StatusCheckSuccessfulResponse]) match {
            case Success(res) =>
              StatusCheckResponseWithStatus(OK, res)
            case Failure(e) =>
              logger.info(s"Invalid json returned in response", e)
              StatusCheckResponseWithStatus(
                INTERNAL_SERVER_ERROR,
                StatusCheckErrorResponse(correlationId, StatusCheckError(UNKNOWN_ERROR))
              )
          }
        case status =>
          Try(response.json.as[StatusCheckErrorResponse]) match {
            case Success(res) =>
              StatusCheckResponseWithStatus(status, res)
            case Failure(_) =>
              StatusCheckResponseWithStatus(
                status,
                StatusCheckErrorResponse(correlationId, StatusCheckError(UNKNOWN_ERROR))
              )
          }
      }
    }

  }
}
