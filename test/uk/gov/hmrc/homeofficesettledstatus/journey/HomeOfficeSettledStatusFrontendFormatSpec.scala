/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatus.journey

import java.time.LocalDate

import play.api.libs.json.{Format, JsResultException, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State.{MultipleMatchesFound, Start, StatusCheckByNino, StatusCheckFailure, StatusFound}
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyStateFormats
import uk.gov.hmrc.homeofficesettledstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckError, StatusCheckResult, ValidationError}
import uk.gov.hmrc.play.test.UnitSpec

class HomeOfficeSettledStatusFrontendFormatSpec extends UnitSpec {

  implicit val formats: Format[State] = HomeOfficeSettledStatusFrontendJourneyStateFormats.formats

  "HomeOfficeSettledStatusFrontendJourneyStateFormats" should {
    "serialize and deserialize state" when {
      "Start" in {
        val state = Start

        val json = Json.parse("""{"state":"Start"}""")
        Json.toJson(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusCheckByNino without query" in {
        val state = StatusCheckByNino(None)

        val json = Json.parse("""{"state":"StatusCheckByNino","properties":{}}""")
        Json.toJson(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusCheckByNino with query" in {
        val state =
          StatusCheckByNino(
            Some(StatusCheckByNinoRequest("1956-05-08", "bar", "foo", Nino("RJ301829A"))))

        val json = Json.parse(
          """{"state":"StatusCheckByNino","properties":{"maybeQuery":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"}}}""")
        Json.toJson(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusFound" in {
        val state = StatusFound(
          correlationId = "1234567890",
          query = StatusCheckByNinoRequest("1956-05-08", "bar", "foo", Nino("RJ301829A")),
          result = StatusCheckResult(
            LocalDate.parse("1956-05-08"),
            "string",
            "Foo Bar",
            List(ImmigrationStatus("ILR", true, Some(LocalDate.parse("2001-01-01")), None)))
        )

        val json = Json.parse(
          """{"state":"StatusFound","properties":{"correlationId":"1234567890","query":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"},"result":{"dateOfBirth":"1956-05-08","facialImage":"string","fullName":"Foo Bar","statuses":[{"immigrationStatus":"ILR","rightToPublicFunds":true,"statusStartDate":"2001-01-01"}]}}}""")
        Json.toJson(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusCheckFailure" in {
        val state = StatusCheckFailure(
          correlationId = "1234567890",
          query = StatusCheckByNinoRequest("1956-05-08", "bar", "foo", Nino("RJ301829A")),
          error = StatusCheckError(Some("FOO_ERROR"), Some(List(ValidationError("code1", "name1"))))
        )

        val json = Json.parse(
          """{"state":"StatusCheckFailure","properties":{"correlationId":"1234567890","query":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"},"error":{"errCode":"FOO_ERROR","fields":[{"code":"code1","name":"name1"}]}}}""")
        Json.toJson(state) shouldBe json
        json.as[State] shouldBe state
      }

      "MultipleMatchesFound" in {
        val state = MultipleMatchesFound(
          correlationId = "1234567890",
          query = StatusCheckByNinoRequest("1956-05-08", "bar", "foo", Nino("RJ301829A"))
        )

        val json = Json.parse(
          """{"state":"MultipleMatchesFound","properties":{"correlationId":"1234567890","query":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"}}}""")
        Json.toJson(state) shouldBe json
        json.as[State] shouldBe state
      }
    }

    "throw an exception when unknown state" in {
      val json = Json.parse("""{"state":"StrangeState","properties":{}}""")
      an[JsResultException] shouldBe thrownBy {
        json.as[State]
      }
    }

  }
}
