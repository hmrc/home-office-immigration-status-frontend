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

package uk.gov.hmrc.homeofficeimmigrationstatus.config

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import scala.concurrent.duration._

class AppConfigSpec extends PlaySpec with GuiceOneAppPerSuite {

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "Application configuration" when {

    "contains correct configured values" must {

      "host" in {
        appConfig.appName mustBe "home-office-immigration-status-frontend"
      }

      "authBaseUrl" in {
        appConfig.authBaseUrl mustBe "http://localhost:8500"
      }

<<<<<<< HEAD:test/uk/gov/hmrc/homeofficesettledstatus/config/AppConfigSpec.scala
      "homeOfficeSettledStatusProxyBaseUrl" in {
=======
      "homeOfficeImmigrationStatusProxyBaseUrl" in {
>>>>>>> HOSS2-149 - Update all references of settled status to immigration status:test/uk/gov/hmrc/homeofficeimmigrationstatus/config/AppConfigSpec.scala
        appConfig.homeOfficeImmigrationStatusProxyBaseUrl mustBe "http://localhost:10211"
      }

      "mongoSessionExpiryTime" in {
        appConfig.mongoSessionExpiration mustBe 1.hour
      }

      "authorisedStrideGroup" in {
        appConfig.authorisedStrideGroup mustBe "TBC"
      }

      "defaultQueryTimeRangeInMonths" in {
        appConfig.defaultQueryTimeRangeInMonths mustBe 6
      }

      "gtmId" in {
        appConfig.gtmId mustBe "N/A"
      }

    }
  }
}
