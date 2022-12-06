/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsUndefined, Json}

import java.time.LocalDate

class StatusCheckResultSpec extends PlaySpec {

  def makeImmigrationStatus(daysAgo: Int = 0): ImmigrationStatus =
    ImmigrationStatus(
      LocalDate.now.minusDays(daysAgo),
      None,
      "some product type",
      "some immigration status",
      noRecourseToPublicFunds = true
    )

  "mostRecentStatus" must {
    "return none when there is no immigration status" in {
      val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", Nil)

      sut.mostRecentStatus mustBe None
    }

    "return the immigration status when there is only one immigration status" in {
      val expected = makeImmigrationStatus()
      val sut      = StatusCheckResult("some name", LocalDate.now, "some nationality", List(expected))

      sut.mostRecentStatus mustBe Some(expected)
    }

    "return the most recent immigration status when there is more than one immigration status" in {
      val expected    = makeImmigrationStatus()
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
      val others     = List(3, 1, 2).map(makeImmigrationStatus)

      val sut = StatusCheckResult("some name", LocalDate.now, "some nationality", mostRecent +: others)

      val expected = List(1, 2, 3).map(makeImmigrationStatus)

      withClue("the status are also sorted in start date order") {
        sut.previousStatuses mustBe expected
      }
    }
  }

  "auditWrites" must {

    "write json with mostRecentStatus and previous status" in {
      val date             = LocalDate.now
      val mostRecentStatus = makeImmigrationStatus()
      val previousStatuses = Seq(makeImmigrationStatus(100), makeImmigrationStatus(1000)) //scalastyle:off magic.number
      val result =
        StatusCheckResult("some name", date, "some nationality", (previousStatuses :+ mostRecentStatus).toList)

      val resultJson = Json.toJson(result)(StatusCheckResult.auditWrites)

      (resultJson \ "mostRecentStatus").get mustEqual Json.toJson(mostRecentStatus)
      (resultJson \ "previousStatuses").get mustEqual Json.toJson(previousStatuses)
      resultJson \ "statuses" mustBe a[JsUndefined]
    }

    "write json with mostRecentStatus" in {
      val date             = LocalDate.now
      val mostRecentStatus = makeImmigrationStatus()
      val result =
        StatusCheckResult("some name", date, "some nationality", List(mostRecentStatus))

      val resultJson = Json.toJson(result)(StatusCheckResult.auditWrites)

      (resultJson \ "mostRecentStatus").get mustEqual Json.toJson(mostRecentStatus)
      (resultJson \ "previousStatuses").get mustEqual Json.toJson(List.empty[ImmigrationStatus])
      resultJson \ "statuses" mustBe a[JsUndefined]
    }

    "write json without mostRecentStatus and previous status" in {
      val date = LocalDate.now
      val result =
        StatusCheckResult("some name", date, "some nationality", Nil)

      val resultJson = Json.toJson(result)(StatusCheckResult.auditWrites)

      resultJson \ "mostRecentStatus" mustBe a[JsUndefined]
      (resultJson \ "previousStatuses").get mustEqual Json.toJson(List.empty[ImmigrationStatus])
      resultJson \ "statuses" mustBe a[JsUndefined]
    }

  }
}
