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

import org.mockito.ArgumentMatchers.{any, refEq, eq => is}
import org.mockito.Mockito._
import utils.NinoGenerator
import java.time.{LocalDate, ZoneId}
import java.net.URL
import models._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import play.api.inject.bind
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import config.AppConfig
import uk.gov.hmrc.http._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Writes
import services.SessionCacheService
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.AnyContentAsEmpty
import scala.util.Try
import org.mockito.internal.util.reflection.Whitebox

class HomeOfficeImmigrationStatusProxyConnectorSpec
    extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  val mockAppConfig = mock(classOf[AppConfig])
  when(mockAppConfig.homeOfficeImmigrationStatusProxyBaseUrl).thenReturn("http://localhost:1234")
  val mockHttpClient = mock(classOf[HttpClient])
  val mockSessionCacheService: SessionCacheService = mock(classOf[SessionCacheService])

  override def beforeEach(): Unit = {
    reset(mockHttpClient)
    reset(mockAppConfig)
    super.beforeEach()
  }

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[HttpClient].toInstance(mockHttpClient),
      bind[AppConfig].toInstance(mockAppConfig),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  implicit val fakeReq: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(fakeReq)

  lazy val sut = inject[HomeOfficeImmigrationStatusProxyConnector]

  val ninoRequest = NinoSearch(
    NinoGenerator.generateNino,
    "Name",
    "Full",
    LocalDate.now.toString,
    StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
  )

  val mrzRequest = MrzSearch(
    "documentType",
    "documentNumber",
    LocalDate.now,
    "nationality",
    StatusCheckRange(Some(LocalDate.now(ZoneId.of("UTC")).minusMonths(1)), Some(LocalDate.now(ZoneId.of("UTC"))))
  )

  val correlationId = Some("some-correlation-id")

  "statusPublicFundsByNino" should {
    "send a HTTP request with the correct body" in {
      val url = new URL("http://localhost:1234/v1/status/public-funds/nino").toExternalForm
      val response =
        Right(StatusCheckSuccessfulResponse(correlationId, StatusCheckResult("Full Name", LocalDate.now, "USA", Nil)))

      when(
        mockHttpClient.POST(any(), any(), any[Seq[(String, String)]])(
          any[Writes[NinoSearch]],
          any[HttpReads[Either[StatusCheckError, StatusCheckSuccessfulResponse]]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(response))

      sut.statusPublicFundsByNino(ninoRequest)

      verify(mockHttpClient).POST(refEq(url), refEq(ninoRequest), any())(any(), any(), any(), any())

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
          any[ExecutionContext]))
        .thenReturn(Future.successful(response))

      sut.statusPublicFundsByMrz(mrzRequest)

      verify(mockHttpClient).POST(refEq(url), refEq(mrzRequest), any())(any(), any(), any(), any())

    }
  }

}
