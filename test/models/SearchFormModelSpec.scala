/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.domain.Nino
import utils.NinoGenerator

import java.time.LocalDate

class SearchFormModelSpec extends AnyWordSpecLike with Matchers {

  val nino: Nino             = NinoGenerator.generateNino
  val dateOfBirth: LocalDate = LocalDate.now().minusDays(1)

  "NinoSearchFormModel" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val form = NinoSearchFormModel(nino, "first", "last", dateOfBirth)
        Json.toJson(form) shouldBe Json.obj(
          "nino"        -> s"$nino",
          "givenName"   -> "first",
          "familyName"  -> "last",
          "dateOfBirth" -> s"$dateOfBirth"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "nino"        -> s"$nino",
          "givenName"   -> "first",
          "familyName"  -> "last",
          "dateOfBirth" -> s"$dateOfBirth"
        )
        json.validate[NinoSearchFormModel] shouldBe JsSuccess(
          NinoSearchFormModel(nino, "first", "last", dateOfBirth)
        )
      }
    }

    "when one field is missing" in {
      val json = Json.obj(
        "nino"        -> s"$nino",
        "givenName"   -> "first",
        "dateOfBirth" -> s"$dateOfBirth"
      )
      json.validate[NinoSearchFormModel] shouldBe a[JsError]
    }

    "fields are empty" in {
      val json = Json.obj()
      json.validate[NinoSearchFormModel] shouldBe a[JsError]
    }

    "invalid field types" in {
      val json = Json.obj(
        "nino"        -> 0,
        "givenName"   -> 0,
        "familyName"  -> 0,
        "dateOfBirth" -> 0
      )
      json.validate[NinoSearchFormModel] shouldBe a[JsError]
    }
  }

  "MrzSearchFormModel" should {
    "serialize to JSON" when {
      "all fields are defined" in {
        val form = MrzSearchFormModel("DocType", "12345", dateOfBirth, "nationality")
        Json.toJson(form) shouldBe Json.obj(
          "documentType"   -> "DocType",
          "documentNumber" -> "12345",
          "dateOfBirth"    -> s"$dateOfBirth",
          "nationality"    -> "nationality"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are defined" in {
        val json = Json.obj(
          "documentType"   -> "DocType",
          "documentNumber" -> "12345",
          "dateOfBirth"    -> s"$dateOfBirth",
          "nationality"    -> "nationality"
        )
        json.validate[MrzSearchFormModel] shouldBe JsSuccess(
          MrzSearchFormModel("DocType", "12345", dateOfBirth, "nationality")
        )
      }
    }

    "when the document number is missing" in {
      val json = Json.obj(
        "documentType" -> "DocType",
        "dateOfBirth"  -> s"$dateOfBirth",
        "nationality"  -> "nationality"
      )
      json.validate[MrzSearchFormModel] shouldBe a[JsError]
    }

    "fields are empty" in {
      val json = Json.obj()
      json.validate[MrzSearchFormModel] shouldBe a[JsError]
    }

    "invalid field types" in {
      val json = Json.obj(
        "documentType"   -> 0,
        "documentNumber" -> 12345,
        "dateOfBirth"    -> 0,
        "nationality"    -> 0
      )
      json.validate[MrzSearchFormModel] shouldBe a[JsError]
    }
  }
}
