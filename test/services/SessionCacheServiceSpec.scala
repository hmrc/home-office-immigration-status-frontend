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

package services

import models.{FormQueryModel, NinoSearchFormModel}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.NinoGenerator
import crypto.{FormModelEncrypter, SecureGCMCipher, TestGCMCipher}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.test.Injecting
import config.AppConfig

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class SessionCacheServiceSpec
    extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach with ScalaFutures {

  val now = LocalDateTime.now
  val mockRepo = mock(classOf[SessionCacheRepository])
  private val cipher = new TestGCMCipher
  private val encrypter = new FormModelEncrypter(cipher)
  lazy val appConfig: AppConfig = inject[AppConfig]
  val sut = new SessionCacheServiceImpl(mockRepo, encrypter, appConfig)

  override def beforeEach(): Unit = {
    reset(mockRepo)
    super.beforeEach
  }

  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  val formModel = NinoSearchFormModel(
    nino = NinoGenerator.generateNino,
    givenName = "Jimmy",
    familyName = "Jazz",
    dateOfBirth = LocalDate.now)
  val encryptedFormModel = encrypter.encryptSearchFormModel(formModel, "123", secretKey)
  val formQuery = FormQueryModel(id = "123", data = encryptedFormModel)

  "get" must {

    "check the repository and return none where the header carrier has a session id" in {
      when(mockRepo.findById(any(), any())(any())).thenReturn(Future.successful(None))
      val hc = HeaderCarrier(sessionId = Some(SessionId("123")))
      val result = Await.result(sut.get(hc, implicitly), 5 seconds)
      result mustBe None
      verify(mockRepo).findById(refEq("123"), any())(any())
    }

    "check the repository and return some where the header carrier has a session id" in {
      when(mockRepo.findById(any(), any())(any())).thenReturn(Future.successful(Some(formQuery)))
      val hc = HeaderCarrier(sessionId = Some(SessionId("123")))
      val result = Await.result(sut.get(hc, implicitly), 5 seconds)
      result mustBe Some(formModel)
      verify(mockRepo).findById(refEq("123"), any())(any())
    }

    "return an error where the header carrier has no session id" in {
      val hc = HeaderCarrier(sessionId = None)
      intercept[NoSessionIdException.type] { Await.result(sut.get(hc, implicitly), 5 seconds) }
      verify(mockRepo, never()).findById(any(), any())(any())
    }
  }

  "set" must {

    "call findAndUpdate in the repo" in {
      val selector = Json.obj("_id"  -> formQuery.id)
      val modifier = Json.obj("$set" -> (formQuery copy (lastUpdated = now)))
      val hc = HeaderCarrier(sessionId = Some(SessionId("123")))

      val mockReturn = mock(
        classOf[reactivemongo.api.commands.FindAndModifyCommand.Result[
          SessionCacheServiceSpec.this.mockRepo.collection.pack.type]])

      when(mockRepo.findAndUpdate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(mockReturn))

      Await.result(sut.set(formModel, now)(hc, implicitly), 5 seconds)
      verify(mockRepo)
        .findAndUpdate(refEq(selector), refEq(modifier), any(), any(), any(), any(), any(), any(), any(), any(), any())(
          any())
    }

    "return an error where the header carrier has no session id" in {
      val hc = HeaderCarrier(sessionId = None)
      intercept[NoSessionIdException.type] { Await.result(sut.set(formModel, now)(hc, implicitly), 5 seconds) }
      verify(mockRepo, never)
        .findAndUpdate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())(any())
    }
  }

  "delete" must {

    "call removeById where the header carrier has a session id" in {
      val mockResponse = mock(classOf[reactivemongo.api.commands.WriteResult])
      when(mockRepo.removeById(any(), any())(any())).thenReturn(Future.successful(mockResponse))
      val hc = HeaderCarrier(sessionId = Some(SessionId("123")))
      Await.result(sut.delete(hc, implicitly), 5 seconds)
      verify(mockRepo).removeById(refEq("123"), any())(any())
    }

    "return an error where the header carrier has no session id" in {
      val hc = HeaderCarrier(sessionId = None)
      intercept[NoSessionIdException.type] { Await.result(sut.delete(hc, implicitly), 5 seconds) }
      verify(mockRepo, never()).removeById(any(), any())(any())
    }
  }
}
