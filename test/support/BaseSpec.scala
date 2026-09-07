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

import org.apache.pekko.util.Timeout
import org.mockito.Mockito.mock
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.Injecting
import repositories.SessionCacheRepository
import uk.gov.hmrc.mongo.play.PlayMongoModule
import scala.concurrent.duration.*
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Awaitable}
import scala.language.postfixOps

trait BaseSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with BeforeAndAfterEach
    with Matchers
    with OptionValues {

  private val timeoutDuration: FiniteDuration                         = 5 seconds
  protected implicit val timeout: Timeout                             = Timeout(timeoutDuration)
  protected def await[T](future: Awaitable[T]): T                     = Await.result(future, timeoutDuration)
    
  protected val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  protected val modules: Seq[GuiceableModule] = Seq(
    bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
  )

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(modules*)
      .disable[PlayMongoModule]

  override implicit lazy val app: Application = appBuilder.build()
}
