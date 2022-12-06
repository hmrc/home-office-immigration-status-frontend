/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import com.kenshoo.play.metrics.Metrics
import config.AppConfig
import models._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Injecting}
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.{HeaderCarrier, _}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.NinoGenerator

import java.net.URL
import java.time.{LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class HomeOfficeImmigrationStatusProxyConnectorSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with BeforeAndAfterEach {

  val mockAppConfig: AppConfig = mock(classOf[AppConfig])
  when(mockAppConfig.homeOfficeImmigrationStatusProxyBaseUrl).thenReturn("http://localhost:1234")
  val mockHttpClient: HttpClient                      = mock(classOf[HttpClient])
  val mockSessionCacheService: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[HttpClient].toInstance(mockHttpClient),
      bind[AppConfig].toInstance(mockAppConfig),
      bind[SessionCacheRepository].toInstance(mockSessionCacheService)
    )
    .build()

  override def beforeEach(): Unit = {
    reset(mockHttpClient)
    reset(mockAppConfig)
    super.beforeEach()
  }

  implicit val fakeReq: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc: HeaderCarrier                            = HeaderCarrierConverter.fromRequest(fakeReq)

  lazy val sut: HomeOfficeImmigrationStatusProxyConnector = inject[HomeOfficeImmigrationStatusProxyConnector]

  val ninoRequest: NinoSearch = NinoSearch(
    NinoGenerator.generateNino,
    "Name",
    "Full",
    LocalDate.now.toString,
    StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
  )

  val mrzRequest: MrzSearch = MrzSearch(
    "documentType",
    "documentNumber",
    LocalDate.now,
    "nationality",
    StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
  )

  val correlationId: Some[String] = Some("some-correlation-id")

  lazy val metrics: Metrics = mock(classOf[Metrics])

  trait Setup {
    val uuid = "123f4567-g89c-42c3-b456-557742330000"
    val connector: HomeOfficeImmigrationStatusProxyConnector =
      new HomeOfficeImmigrationStatusProxyConnector(mockAppConfig, mockHttpClient, metrics) {
        override def generateNewUUID: String = uuid
      }
  }

  "statusPublicFundsByNino" should {
    "send a HTTP request with the correct body" in {
      val url = new URL("http://localhost:1234/v1/status/public-funds/nino").toExternalForm
      val response =
        Right(StatusCheckSuccessfulResponse(correlationId, StatusCheckResult("Full Name", LocalDate.now, "USA", Nil)))
      val capture: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])

      when(
        mockHttpClient.POST(any(), any(), any[Seq[(String, String)]])(
          any[Writes[NinoSearch]],
          any[HttpReads[Either[StatusCheckError, StatusCheckSuccessfulResponse]]],
          capture.capture(),
          any[ExecutionContext]
        )
      )
        .thenReturn(Future.successful(response))

      sut.statusPublicFundsByNino(ninoRequest)

      verify(mockHttpClient).POST(refEq(url), refEq(ninoRequest), any())(any(), any(), any(), any())
      capture.getValue.extraHeaders.size mustBe 1
      capture.getValue.extraHeaders.filter(h => h._1.equals("CorrelationId")).map(h => h._2).orElse("") mustNot be("")

    }
  }

  "statusPublicFundsByMrz" should {
    "send a HTTP request with the correct body" in {
      val url = new URL("http://localhost:1234/v1/status/public-funds/mrz").toExternalForm
      val response =
        Right(StatusCheckSuccessfulResponse(correlationId, StatusCheckResult("Full Name", LocalDate.now, "USA", Nil)))

      when(
        mockHttpClient.POST(any(), any(), any[Seq[(String, String)]])(
          any[Writes[MrzSearch]],
          any[HttpReads[Either[StatusCheckError, StatusCheckSuccessfulResponse]]],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(Future.successful(response))

      sut.statusPublicFundsByMrz(mrzRequest)

      verify(mockHttpClient).POST(refEq(url), refEq(mrzRequest), any())(any(), any(), any(), any())

    }
  }

  "requestID is present in the headerCarrier" should {
    "return new ID pre-appending the requestID when the requestID matches the format(8-4-4-4)" in new Setup {
      val requestId  = "dcba0000-ij12-df34-jk56"
      val uuidLength = 24
      connector.correlationId(HeaderCarrier(requestId = Some(RequestId(requestId)))) mustBe
        s"$requestId-${uuid.substring(uuidLength)}"
    }

    "return new ID when the requestID does not match the format(8-4-4-4)" in new Setup {
      val requestId = "1a2b-ij12-df34-jk56"
      connector.correlationId(HeaderCarrier(requestId = Some(RequestId(requestId)))) mustBe uuid
    }
  }

  "requestID is not present in the headerCarrier should return a new ID" should {
    "return the uuid" in new Setup {
      connector.correlationId(HeaderCarrier()) mustBe uuid
    }
  }

}
