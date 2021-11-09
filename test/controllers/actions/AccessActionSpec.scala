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

package controllers.actions

import controllers.ControllerSpec
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.Ok
import play.api.mvc.{ActionFunction, Request, Result}

import scala.concurrent.Future

class AccessActionSpec extends ControllerSpec {

  val mockShutterAction: ShutterAction = mock(classOf[ShutterAction])
  val mockAuthAction: AuthAction = mock(classOf[AuthAction])

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockShutterAction, mockAuthAction)
  }

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].toInstance(mockAuthAction),
      bind[ShutterAction].toInstance(mockShutterAction)
    )
    .build()

  val expected = Ok("Invoked")
  val testBlock: Request[_] => Future[Result] = _ => Future.successful(expected)

  "invokeBlock" must {
    "compose the shutter and auth actions" in {
      val mockComposed = mock(classOf[ActionFunction[Request, Request]])
      when(mockShutterAction.andThen(mockAuthAction)).thenReturn(mockComposed)
      when(mockComposed.invokeBlock(request, testBlock)).thenReturn(Future.successful(expected))

      val result = await(inject[AccessAction].async(testBlock)(request))

      result mustBe expected
      verify(mockComposed).invokeBlock(request, testBlock)
    }
  }

}
