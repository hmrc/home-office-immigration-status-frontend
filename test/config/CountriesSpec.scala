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

package config

import com.typesafe.config.ConfigException
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import play.api.{Application, Environment}
import repositories.SessionCacheRepository

import java.io.InputStream

class CountriesSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .build()

  val mockEnv: Environment  = mock(classOf[Environment])
  lazy val env: Environment = inject[Environment]

  override protected def beforeEach(): Unit = {
    reset(mockEnv)
    mockActualEnvFile("location-autocomplete-canonical-list.json")
    mockActualEnvFile("ISO_3166-alpha3-alpha2-numeric.json")
    super.beforeEach()
  }

  "Countries" should {
    "throw an exception" when {
      "the canonical list file is not found" in {
        when(mockEnv.resourceAsStream(ArgumentMatchers.eq("location-autocomplete-canonical-list.json")))
          .thenReturn(None)

        val caught = intercept[ConfigException.BadValue] {
          new Countries(mockEnv)
        }

        caught.getMessage mustEqual "Invalid value at 'location-autocomplete-canonical-list.json': Alpha2 to Name map cannot be constructed."
      }

      "the iso3 file is not found" in {
        when(mockEnv.resourceAsStream(ArgumentMatchers.eq("ISO_3166-alpha3-alpha2-numeric.json"))).thenReturn(None)

        val caught = intercept[ConfigException.BadValue] {
          new Countries(mockEnv)
        }

        caught.getMessage mustEqual "Invalid value at 'ISO_3166-alpha3-alpha2-numeric.json': ISO codes json does not exist"
      }
    }
  }

  def mockActualEnvFile(filename: String): OngoingStubbing[Option[InputStream]] =
    when(mockEnv.resourceAsStream(ArgumentMatchers.eq(filename))).thenReturn(env.resourceAsStream(filename))

}
