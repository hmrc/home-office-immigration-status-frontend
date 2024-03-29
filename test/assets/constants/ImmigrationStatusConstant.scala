/*
 * Copyright 2024 HM Revenue & Customs
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

package assets.constants

import models.ImmigrationStatus

import java.time.LocalDate

object ImmigrationStatusConstant {

  val validStatus: ImmigrationStatus = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2021-01-01"),
    statusEndDate = Some(LocalDate.now().plusDays(1)),
    productType = "EUS",
    immigrationStatus = "ILR",
    noRecourseToPublicFunds = false
  )

  val validStatusNoResourceTrue: ImmigrationStatus = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2021-01-01"),
    statusEndDate = None,
    productType = "EUS",
    immigrationStatus = "ILR",
    noRecourseToPublicFunds = true
  )

  def validStatusCustomProductType(typeName: String): ImmigrationStatus = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2021-01-01"),
    statusEndDate = None,
    productType = typeName,
    immigrationStatus = "ILR",
    noRecourseToPublicFunds = true
  )

  def validStatusCustomImmigrationStatus(typeName: String, status: String): ImmigrationStatus = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2021-01-01"),
    statusEndDate = None,
    productType = typeName,
    immigrationStatus = status,
    noRecourseToPublicFunds = true
  )

}
