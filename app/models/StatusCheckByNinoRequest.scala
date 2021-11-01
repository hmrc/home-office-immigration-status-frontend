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
import uk.gov.hmrc.domain.Nino

case class StatusCheckByNinoRequest(
  // National insurance number
  nino: Nino,
  // Given name required for search
  givenName: String,
  // Family name required for search
  familyName: String,
  // Date of birth of the person being checked in ISO 8601 format (can contain wildcards for day or month)
  dateOfBirth: String,
  // Status check range, default to 6 months back
  statusCheckRange: StatusCheckRange
) {

  def toUpperCase: StatusCheckByNinoRequest =
    copy(givenName = this.givenName.toUpperCase, familyName = this.familyName.toUpperCase)

}

object StatusCheckByNinoRequest {
  implicit val formats: OFormat[StatusCheckByNinoRequest] = Json.format[StatusCheckByNinoRequest]
}
