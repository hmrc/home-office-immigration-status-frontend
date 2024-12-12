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

package connectors

import config.AppConfig
import connectors.Constants._
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.SessionCacheRepository
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HomeOfficeImmigrationStatusProxyConnectorSpec extends PlaySpec with ScalaFutures with BeforeAndAfterEach {

  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  val mockRequestBuilder: RequestBuilder              = mock(classOf[RequestBuilder])
  val mockHttpClient: HttpClientV2                    = mock(classOf[HttpClientV2])
  val mockSessionCacheService: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient)
    reset(mockAppConfig)
    reset(mockRequestBuilder)
  }

  val mockBaseUrl     = "http://localhost:1234"
  val ninoUrl: String = s"$mockBaseUrl/v1/status/public-funds/nino"
  val mrzUrl: String  = s"$mockBaseUrl/v1/status/public-funds/mrz"

  private class EndpointTestSetup(url: String) {

    private val now: LocalDate = LocalDate.now

    val response: StatusCheckResponseWithStatus =
      StatusCheckResponseWithStatus(
        200,
        StatusCheckSuccessfulResponse(Some(correlationId), StatusCheckResult("Full Name", now, "USA", Nil))
      )

    lazy val sut: HomeOfficeImmigrationStatusProxyConnector =
      new HomeOfficeImmigrationStatusProxyConnector(mockAppConfig, mockHttpClient)

    val capture: ArgumentCaptor[HeaderCarrier] = ArgumentCaptor.forClass(classOf[HeaderCarrier])

    when(mockRequestBuilder.setHeader(any()))
      .thenReturn(mockRequestBuilder)

    when(mockRequestBuilder.withBody(any())(using any(), any(), any()))
      .thenReturn(mockRequestBuilder)

    when(mockRequestBuilder.execute(using any[HttpReads[StatusCheckResponseWithStatus]], any()))
      .thenReturn(Future(response))

    when(mockAppConfig.homeOfficeImmigrationStatusProxyBaseUrl)
      .thenReturn(mockBaseUrl)

    when(mockHttpClient.post(ArgumentMatchers.eq(url"$url"))(capture.capture()))
      .thenReturn(mockRequestBuilder)

    implicit val fakeReq: FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest().withHeaders("CorrelationId" -> correlationId)
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(fakeReq)
  }

  private trait CorrelationIdTestSetup {
    val uuid = "123f4567-g89c-42c3-b456-557742330000"
    val connector: HomeOfficeImmigrationStatusProxyConnector =
      new HomeOfficeImmigrationStatusProxyConnector(mockAppConfig, mockHttpClient) {
        override def generateNewUUID: String = uuid
      }
  }

  "statusPublicFundsByNino" should {

    "send a HTTP request with the correct body" in new EndpointTestSetup(ninoUrl) {

      val actual: Future[StatusCheckResponseWithStatus] = sut.statusPublicFundsByNino(ninoRequest)

      actual.futureValue mustBe response

      verify(mockRequestBuilder, times(1))
        .execute(using any(), any())

      capture.getValue.headers(Seq("CorrelationId")) mustBe Seq("CorrelationId" -> "some-correlation-id")
    }
  }

  "statusPublicFundsByMrz" should {

    "send a HTTP request with the correct body" in new EndpointTestSetup(mrzUrl) {

      val actual: Future[StatusCheckResponseWithStatus] = sut.statusPublicFundsByMrz(mrzRequest)

      actual.futureValue mustBe response

      verify(mockRequestBuilder, times(1))
        .execute(using any(), any())

      capture.getValue.headers(Seq("CorrelationId")) mustBe Seq("CorrelationId" -> "some-correlation-id")
    }
  }

  "requestID is present in the headerCarrier" should {
    "return new ID pre-appending the requestID when the requestID matches the format(8-4-4-4)" in new CorrelationIdTestSetup {
      val requestId  = "dcba0000-ij12-df34-jk56"
      val uuidLength = 24
      connector.correlationId(HeaderCarrier(requestId = Some(RequestId(requestId)))) mustBe
        s"$requestId-${uuid.substring(uuidLength)}"
    }

    "return new ID when the requestID does not match the format(8-4-4-4)" in new CorrelationIdTestSetup {
      val requestId = "1a2b-ij12-df34-jk56"
      connector.correlationId(HeaderCarrier(requestId = Some(RequestId(requestId)))) mustBe uuid
    }
  }

  "requestID is not present in the headerCarrier should return a new ID" should {
    "return the uuid" in new CorrelationIdTestSetup {
      connector.correlationId(HeaderCarrier()) mustBe uuid
    }
  }

}
