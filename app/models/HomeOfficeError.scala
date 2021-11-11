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

import play.api.http.Status._

sealed abstract class HomeOfficeError(val statusCode: Int)

object HomeOfficeError {
  case object StatusCheckNotFound extends HomeOfficeError(NOT_FOUND)
  case object StatusCheckBadRequest extends HomeOfficeError(BAD_REQUEST)
  case object StatusCheckConflict extends HomeOfficeError(CONFLICT)
  case object StatusCheckInternalServerError extends HomeOfficeError(INTERNAL_SERVER_ERROR)
  case object StatusCheckInvalidResponse extends HomeOfficeError(INTERNAL_SERVER_ERROR)
  case class OtherErrorResponse(override val statusCode: Int) extends HomeOfficeError(statusCode)
}
