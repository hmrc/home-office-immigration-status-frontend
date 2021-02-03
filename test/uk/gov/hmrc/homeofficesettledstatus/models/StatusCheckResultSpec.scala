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

package uk.gov.hmrc.homeofficesettledstatus.models

import java.time.LocalDate

import uk.gov.hmrc.play.test.UnitSpec

class StatusCheckResultSpec extends UnitSpec {

  val expectedResult =
    ImmigrationStatus(LocalDate.parse("2015-02-11"), None, "C", "D", noRecourseToPublicFunds = true)

  "StatusCheckResultSpec" should {

    val formInputWithNoImmigration = StatusCheckResult(
      "A",
      LocalDate.parse("1971-01-01"),
      "B",
      List()
    )

    "return none when there is no immigration status" in {
      formInputWithNoImmigration.mostRecentStatus shouldBe None
    }

    "return the immigration status when there is only one immigration status" in {
      val formInputWithOneImmigrationStatus = StatusCheckResult(
        "A",
        LocalDate.parse("1971-01-01"),
        "B",
        List(expectedResult)
      )

      formInputWithOneImmigrationStatus.mostRecentStatus shouldBe Some(expectedResult)
    }

    "return the most recent immigration status when there is more than one immigration status" in {
      val formInputWithManyImmigrationStatuses = StatusCheckResult(
        "A",
        LocalDate.parse("1971-01-01"),
        "B",
        List(
          ImmigrationStatus(LocalDate.parse("2010-01-02"), None, "A", "B", noRecourseToPublicFunds = false),
          expectedResult)
      )

      formInputWithManyImmigrationStatuses.mostRecentStatus shouldBe Some(expectedResult)

    }
  }

}
