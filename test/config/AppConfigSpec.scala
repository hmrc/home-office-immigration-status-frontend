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

package config

import com.typesafe.config.ConfigFactory
import org.mockito.Mockito._
import org.scalatestplus.play.PlaySpec
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.io.File

class AppConfigSpec extends PlaySpec {

  val mockFile: File               = mock(classOf[File])
  val mockClassLoader: ClassLoader = mock(classOf[ClassLoader])

  def config(runMode: Option[String]): Configuration = {
    val config = Configuration(
      ConfigFactory.parseString(
        s"""
           |appName = "test"
           |isShuttered = false
           |microservice.services.auth.host = "host"
           |microservice.services.auth.port = 12
           |microservice.services.home-office-immigration-status-proxy.host = "host"
           |microservice.services.home-office-immigration-status-proxy.port = 12
           |mongodb.ttl.seconds=1200
           |authorisedStrideGroup="TBC"
           |defaultQueryTimeRangeInMonths=12
           |google-tag-manager.id="123"
           |it.helpdesk.url="URL"
           |httpHeaders.cacheControl="Cache"
           |mongodb.encryption.key="Key123"
      """.stripMargin
      )
    )
    runMode match {
      case Some(mode) => Configuration(ConfigFactory.parseString(s"""run.mode = "$mode" """)).withFallback(config)
      case _          => config
    }
  }

  val devEnv: Environment  = Environment(mockFile, mockClassLoader, Mode.Dev)
  val testEnv: Environment = Environment(mockFile, mockClassLoader, Mode.Test)
  val prodEnv: Environment = Environment(mockFile, mockClassLoader, Mode.Prod)

  "AppConfig" should {
    "return isDevEnv as false" when {

      "env.Mode is set to test and run mode in config is test" in {
        val configuration       = config(Some("Test"))
        val servicesConfig      = new ServicesConfig(configuration)
        lazy val sut: AppConfig = new AppConfig(servicesConfig, configuration, testEnv)
        sut.isDevEnv mustBe false
      }

      "env.Mode is set to test and run mode in config is not test" in {
        val configuration       = config(Some("Dev"))
        val servicesConfig      = new ServicesConfig(configuration)
        lazy val sut: AppConfig = new AppConfig(servicesConfig, configuration, testEnv)
        sut.isDevEnv mustBe false
      }

      "env.Mode is not set to test and run mode in config is not Dev" in {
        val configuration       = config(Some("Test"))
        val servicesConfig      = new ServicesConfig(configuration)
        lazy val sut: AppConfig = new AppConfig(servicesConfig, configuration, devEnv)
        sut.isDevEnv mustBe false
      }

    }

    "return isDevEnv as true" when {

      "env.Mode is not set to test and run mode in config is dev" in {
        val configuration       = config(Some("Dev"))
        val servicesConfig      = new ServicesConfig(configuration)
        lazy val sut: AppConfig = new AppConfig(servicesConfig, configuration, devEnv)
        sut.isDevEnv mustBe true
      }

      "env.Mode is not set to test and run mode in config is not set" in {
        val configuration       = config(None)
        val servicesConfig      = new ServicesConfig(configuration)
        lazy val sut: AppConfig = new AppConfig(servicesConfig, configuration, prodEnv)
        sut.isDevEnv mustBe true
      }

    }

  }

}
