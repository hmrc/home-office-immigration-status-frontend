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

package services

import config.AppConfig
import crypto.FormModelEncrypter
import models.{EncryptedSearchFormModel, FormQueryModel, NinoSearchFormModel, SearchFormModel}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.BaseSpec
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.NinoGenerator

import java.time.{Instant, LocalDate}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class SessionCacheServiceSpec extends BaseSpec with ScalaFutures {

  val now: Instant                                   = Instant.now()
  val argumentCaptor: ArgumentCaptor[FormQueryModel] = ArgumentCaptor.forClass(classOf[FormQueryModel])
  private val encrypter                              = new FormModelEncrypter
  lazy val appConfig: AppConfig                      = app.injector.instanceOf[AppConfig]
  val sut = new SessionCacheServiceImpl(mockSessionCacheRepository, encrypter, appConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionCacheRepository)
  }

  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  val formModel: NinoSearchFormModel = NinoSearchFormModel(
    nino = NinoGenerator.generateNino,
    givenName = "Jimmy",
    familyName = "Jazz",
    dateOfBirth = LocalDate.now
  )
  val encryptedFormModel: EncryptedSearchFormModel = encrypter.encryptSearchFormModel(formModel, "123", secretKey)
  val formQuery: FormQueryModel                    = FormQueryModel(id = "123", data = encryptedFormModel, now)

  "get" must {

    "check the repository and return none where the header carrier has a session id" in {
      when(mockSessionCacheRepository.get(any())(any())).thenReturn(Future.successful(None))
      val hc     = HeaderCarrier(sessionId = Some(SessionId("123")))
      val result = Await.result(sut.get(hc, implicitly), 5 seconds)
      result mustBe None
      verify(mockSessionCacheRepository).get(eqTo("123"))(any())
    }

    "check the repository and return some where the header carrier has a session id" in {
      when(mockSessionCacheRepository.get(any())(any())).thenReturn(Future.successful(Some(formQuery)))
      val hc     = HeaderCarrier(sessionId = Some(SessionId("123")))
      val result = Await.result(sut.get(hc, implicitly), 5 seconds)
      result mustBe Some(formModel)
      verify(mockSessionCacheRepository).get(ArgumentMatchers.eq("123"))(any())
    }

    "return an error where the header carrier has no session id" in {
      val hc = HeaderCarrier(sessionId = None)
      intercept[NoSessionIdException.type](Await.result(sut.get(hc, implicitly), 5 seconds))
      verify(mockSessionCacheRepository, never()).get(any())(any())
    }
  }

  "set" must {

    "call set in the repo" in {
      val hc = HeaderCarrier(sessionId = Some(SessionId("123")))

      when(mockSessionCacheRepository.set(any())(any())).thenReturn(Future.unit)

      Await.result(sut.set(formModel)(hc, implicitly), 5 seconds)
      verify(mockSessionCacheRepository).set(argumentCaptor.capture())(any())

      val form = argumentCaptor.getValue.copy(lastUpdated = now)

      val decryptedFormModel: Option[SearchFormModel] = encrypter.decryptSearchFormModel(form.data, "123", secretKey)

      decryptedFormModel mustBe Some(formModel)
    }

    "return an error where the header carrier has no session id" in {
      val hc = HeaderCarrier(sessionId = None)
      intercept[NoSessionIdException.type](Await.result(sut.set(formModel)(hc, implicitly), 5 seconds))
      verify(mockSessionCacheRepository, never).set(any())(any())
    }
  }

  "delete" must {

    "call delete where the header carrier has a session id" in {
      when(mockSessionCacheRepository.delete(any())(any())).thenReturn(Future.unit)
      val hc = HeaderCarrier(sessionId = Some(SessionId("123")))
      Await.result(sut.delete(hc, implicitly), 5 seconds)
      verify(mockSessionCacheRepository).delete(ArgumentMatchers.eq("123"))(any())
    }

    "return an error where the header carrier has no session id" in {
      val hc = HeaderCarrier(sessionId = None)
      intercept[NoSessionIdException.type](Await.result(sut.delete(hc, implicitly), 5 seconds))
      verify(mockSessionCacheRepository, never()).delete(any())(any())
    }
  }
}
