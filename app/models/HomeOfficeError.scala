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

sealed abstract class HomeOfficeError(val statusCode: Int) {
  def responseBody: String
}

object HomeOfficeError {
  final case class StatusCheckNotFound(responseBody: String) extends HomeOfficeError(NOT_FOUND)
  final case class StatusCheckBadRequest(responseBody: String) extends HomeOfficeError(BAD_REQUEST)
  final case class StatusCheckConflict(responseBody: String) extends HomeOfficeError(CONFLICT)
  final case class StatusCheckInternalServerError(responseBody: String) extends HomeOfficeError(INTERNAL_SERVER_ERROR)
  final case class StatusCheckInvalidResponse(responseBody: String) extends HomeOfficeError(INTERNAL_SERVER_ERROR)
  final case class OtherErrorResponse(override val statusCode: Int, responseBody: String)
      extends HomeOfficeError(statusCode)
}
