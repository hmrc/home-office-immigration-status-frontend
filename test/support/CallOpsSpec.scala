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

package support

import com.typesafe.config._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.{Configuration, Environment, Mode}
import support.CallOps.localFriendlyUrl

import java.io.File

class CallOpsSpec extends AnyWordSpecLike with Matchers with OptionValues {

  val testEnv: Environment    = Environment(new File(""), classOf[CallOpsSpec].getClassLoader, Mode.Test)
  val prodEnv: Environment    = Environment(new File(""), classOf[CallOpsSpec].getClassLoader, Mode.Prod)
  val devEnv: Environment     = Environment(new File(""), classOf[CallOpsSpec].getClassLoader, Mode.Dev)
  val devConf: Configuration  = Configuration(ConfigFactory.parseString(""" run.mode = "Dev" """))
  val prodConf: Configuration = Configuration(ConfigFactory.parseString(""" run.mode = "Prod" """))

  "CallOps" should {

    "return the original url if it is in the test environment" in {
      localFriendlyUrl(testEnv, devConf)("A", "B") shouldBe "A"
    }

    "return url string with localhost and port if is in development environment" in {
      localFriendlyUrl(devEnv, devConf)("A", "B") shouldBe "http://BA"
    }

    "return the original url if it is in the production environment" in {
      localFriendlyUrl(prodEnv, prodConf)("A", "B") shouldBe "A"
    }

    "if url is not absolute then return the url regardless of environment" in {
      localFriendlyUrl(devEnv, devConf)("http://A", "B") shouldBe "http://A"
    }
  }
}
