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

package uk.gov.hmrc.homeofficeimmigrationstatus.journey

import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDate
import play.api.libs.json.{Format, JsResultException, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.journeys.HomeOfficeImmigrationStatusFrontendJourneyModel.State
import uk.gov.hmrc.homeofficeimmigrationstatus.journeys.HomeOfficeImmigrationStatusFrontendJourneyModel.State.{Start, StatusCheckByNino, StatusCheckFailure, StatusFound, StatusNotAvailable}
import uk.gov.hmrc.homeofficeimmigrationstatus.journeys.HomeOfficeImmigrationStatusFrontendJourneyStateFormats
import uk.gov.hmrc.homeofficeimmigrationstatus.models._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers

class HomeOfficeImmigrationStatusFrontendFormatSpec extends AnyWordSpecLike with Matchers with OptionValues {

  implicit val formats: Format[State] = HomeOfficeImmigrationStatusFrontendJourneyStateFormats.formats
  "HomeOfficeImmigrationStatusFrontendJourneyStateFormats" should {
    "serialize and deserialize state" when {
      "Start" in {
        val state = Start
        val json = Json.parse("""{"state":"Start"}""")
        formats.writes(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusCheckByNino without query" in {
        val state = StatusCheckByNino(None)

        val json = Json.parse("""{"state":"StatusCheckByNino","properties":{}}""")
        formats.writes(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusCheckByNino with query" in {
        val state =
          StatusCheckByNino(Some(StatusCheckByNinoRequest(Nino("RJ301829A"), "foo", "bar", "1956-05-08")))

        val json = Json.parse(
          """{"state":"StatusCheckByNino","properties":{"maybeQuery":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"}}}""")
        json.as[State] shouldBe state
      }

      "StatusFound" in {
        val state = StatusFound(
          correlationId = "1234567890",
          query = StatusCheckByNinoRequest(Nino("RJ301829A"), "foo", "bar", "1956-05-08"),
          result = StatusCheckResult(
            "Foo Bar",
            LocalDate.parse("1956-05-08"),
            "IRL",
            List(ImmigrationStatus(LocalDate.parse("2001-01-01"), None, "EUS", "ILR", noRecourseToPublicFunds = true)))
        )

        val json = Json.parse(
          """{"state":"StatusFound","properties":{"correlationId":"1234567890","query":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"},"result":{"fullName":"Foo Bar","dateOfBirth":"1956-05-08","nationality":"IRL","statuses":[{"statusStartDate":"2001-01-01","productType":"EUS","immigrationStatus":"ILR","noRecourseToPublicFunds":true}]}}}""")
        formats.writes(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusNotAvailable" in {
        val state = StatusNotAvailable(
          correlationId = "1234567890",
          query = StatusCheckByNinoRequest(Nino("RJ301829A"), "foo", "bar", "1956-05-08")
        )

        val json = Json.parse(
          """{"state":"StatusNotAvailable","properties":{"correlationId":"1234567890","query":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"}}}""")
        formats.writes(state) shouldBe json
        json.as[State] shouldBe state
      }

      "StatusCheckFailure" in {
        val state = StatusCheckFailure(
          correlationId = "1234567890",
          query = StatusCheckByNinoRequest(Nino("RJ301829A"), "foo", "bar", "1956-05-08"),
          error = StatusCheckError("FOO_ERROR", Some(List(ValidationError("code1", "name1"))))
        )

        val json = Json.parse(
          """{"state":"StatusCheckFailure","properties":{"correlationId":"1234567890","query":{"dateOfBirth":"1956-05-08","familyName":"bar","givenName":"foo","nino":"RJ301829A"},"error":{"errCode":"FOO_ERROR","fields":[{"code":"code1","name":"name1"}]}}}""")
        formats.writes(state) shouldBe json
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
