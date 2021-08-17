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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues
import uk.gov.hmrc.domain.Nino

class StatusCheckByNinoRequestSpec extends AnyWordSpecLike with Matchers with OptionValues {

  val expectedResult: StatusCheckByNinoRequest =
    StatusCheckByNinoRequest(Nino("RJ301829A"), "DOE", "JANE", "1971-01-01", None)

  "StatusCheckByNinoRequestSpec" should {

    val formInputWithNoImmigration = StatusCheckByNinoRequest(
      Nino("RJ301829A"),
      "Doe",
      "Jane",
      "1971-01-01"
    )

    "return givenName and falilyName in uppercase" in {
      formInputWithNoImmigration.toUpperCase shouldBe expectedResult
    }
  }

}
