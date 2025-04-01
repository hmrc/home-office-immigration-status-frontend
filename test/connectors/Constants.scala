/*
 * Copyright 2025 HM Revenue & Customs
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

import models._
import utils.NinoGenerator

import java.time.{LocalDate, ZoneId}

object Constants {

  val correlationId = "some-correlation-id"

  val hoResult: StatusCheckResult =
    StatusCheckResult(
      fullName = "fake full name",
      dateOfBirth = java.time.LocalDate.now(),
      nationality = "lit nation",
      statuses = List(ImmigrationStatus(java.time.LocalDate.now(), None, "", "", noRecourseToPublicFunds = false))
    )

  val ninoRequest: NinoSearch =
    NinoSearch(
      NinoGenerator.generateNino,
      "Name",
      "Full",
      LocalDate.now.toString,
      StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
    )

  val mrzRequest: MrzSearch = MrzSearch(
    "documentType",
    "documentNumber",
    LocalDate.now,
    "nationality",
    StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
  )
}
