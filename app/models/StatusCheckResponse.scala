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

package models

import play.api.libs.json.{Json, OFormat}

final case class StatusCheckResponseWithStatus(statusCode: Int, statusCheckResponse: StatusCheckResponse)

object StatusCheckResponseWithStatus {
  implicit val formats: OFormat[StatusCheckResponseWithStatus] = Json.format[StatusCheckResponseWithStatus]
}

sealed trait StatusCheckResponse {
  def correlationId: Option[String]
}

object StatusCheckResponse {
  implicit val formats: OFormat[StatusCheckResponse] = Json.format[StatusCheckResponse]
}

final case class StatusCheckSuccessfulResponse(
  correlationId: Option[String],
  result: StatusCheckResult
) extends StatusCheckResponse

object StatusCheckSuccessfulResponse {
  implicit val formats: OFormat[StatusCheckSuccessfulResponse] = Json.format[StatusCheckSuccessfulResponse]
}

final case class StatusCheckErrorResponse(
  correlationId: Option[String],
  error: StatusCheckError
) extends StatusCheckResponse

object StatusCheckErrorResponse {
  implicit val formats: OFormat[StatusCheckErrorResponse] = Json.format[StatusCheckErrorResponse]
}
