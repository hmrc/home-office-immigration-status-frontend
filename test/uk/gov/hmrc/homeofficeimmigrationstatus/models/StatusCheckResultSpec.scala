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

package uk.gov.hmrc.homeofficeimmigrationstatus.models

import java.time.LocalDate
import org.scalatestplus.play.PlaySpec

class StatusCheckResultSpec extends PlaySpec {

  def makeImmigrationStatus(daysAgo: Int = 0): ImmigrationStatus =
    ImmigrationStatus(
      LocalDate.now.minusDays(daysAgo),
      None,
      "some product type",
      "some immigration status",
      noRecourseToPublicFunds = true)

  "mostRecentStatus" must {
    "return none when there is no immigration status" in {
      val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", Nil)

      sut.mostRecentStatus mustBe None
    }

    "return the immigration status when there is only one immigration status" in {
      val expected = makeImmigrationStatus()
      val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", List(expected))

      sut.mostRecentStatus mustBe Some(expected)
    }

    "return the most recent immigration status when there is more than one immigration status" in {
      val expected = makeImmigrationStatus()
      val olderStatus = makeImmigrationStatus(1)

      val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", List(olderStatus, expected))

      sut.mostRecentStatus mustBe Some(expected)
    }
  }

  "previousStatuses" must {
    "return an empty list" when {
      "there are no immigration statuses" in {
        val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", Nil)

        sut.previousStatuses mustBe Nil
      }
      "there is only 1 immigration status" in {
        val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", List(makeImmigrationStatus()))

        sut.previousStatuses mustBe Nil
      }
    }
    "return everything but the most recent status" in {
      val mostRecent = makeImmigrationStatus()
      val others = List(3, 1, 2).map(makeImmigrationStatus)

      val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", mostRecent +: others)

      val expected = List(1, 2, 3).map(makeImmigrationStatus)

      withClue("the status are also sorted in start date order") {
        sut.previousStatuses mustBe expected
      }
    }
  }
}
