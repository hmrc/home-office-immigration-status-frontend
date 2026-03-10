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
import connectors.HomeOfficeImmigrationStatusProxyConnector
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import repositories.SessionCacheRepository
import support.BaseSpec
import uk.gov.hmrc.mongo.play.PlayMongoModule
import utils.NinoGenerator
import play.api.test.FakeRequest
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

class HomeOfficeImmigrationStatusProxyServiceSpec extends BaseSpec {
  private val mockAuditService: AuditService = mock(classOf[AuditService])
  private val mockConnector: HomeOfficeImmigrationStatusProxyConnector = mock(
    classOf[HomeOfficeImmigrationStatusProxyConnector]
  )

  override protected def beforeEach(): Unit = {
    reset(mockAuditService)
    reset(mockConnector)
    super.beforeEach()
  }

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuditService].toInstance(mockAuditService),
      bind[HomeOfficeImmigrationStatusProxyConnector].toInstance(mockConnector),
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .disable[PlayMongoModule]
    .build()

  private lazy val sut: HomeOfficeImmigrationStatusProxyService =
    app.injector.instanceOf[HomeOfficeImmigrationStatusProxyService]

  private val testDate: LocalDate = LocalDate.now
  private val formModel: NinoSearchFormModel =
    NinoSearchFormModel(NinoGenerator.generateNino, "Doe", "Jane", LocalDate.of(2001, 1, 31))
  private val mrzSearchFormModel: MrzSearchFormModel =
    MrzSearchFormModel("PASSPORT", "123456", LocalDate.of(2001, 1, 31), "USA")
  private implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  private implicit def hc(implicit request: FakeRequest[?]): HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
  private lazy val appConfig: AppConfig                             = app.injector.instanceOf[AppConfig]  
  given conf: AppConfig     = appConfig

  private val statusCheckResult: StatusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
  private val result: StatusCheckResponseWithStatus =
    StatusCheckResponseWithStatus(200, StatusCheckSuccessfulResponse(Some("CorrelationId"), statusCheckResult))

  "statusPublicFundsByNino" must {
    "only access the audit service when the call downstream was successful" in {
      doNothing().when(mockAuditService).auditStatusCheckEvent(any(), any())(any(), any(), any())
      when(mockConnector.statusPublicFundsByNino(any())(any(), any())).thenReturn(Future.successful(result))

      await(sut.search(formModel))
      verify(mockAuditService).auditStatusCheckEvent(any(), any())(any(), any(), any())
    }

    "don't access the audit service when the call downstream was not successful" in {
      doNothing().when(mockAuditService).auditStatusCheckEvent(any(), any())(any(), any(), any())
      when(mockConnector.statusPublicFundsByNino(any())(any(), any()))
        .thenReturn(Future.failed(new Exception("It went wrong")))

      intercept[Exception](await(sut.search(formModel)))
      verify(mockAuditService, never).auditStatusCheckEvent(any(), any())(any(), any(), any())
    }

  }

  "statusPublicFundsByMrz" must {
    "only access the audit service when the call downstream was successful" in {
      doNothing().when(mockAuditService).auditStatusCheckEvent(any(), any())(any(), any(), any())
      when(mockConnector.statusPublicFundsByMrz(any())(any(), any())).thenReturn(Future.successful(result))

      await(sut.search(mrzSearchFormModel))
      verify(mockAuditService).auditStatusCheckEvent(any(), any())(any(), any(), any())
    }

    "don't access the audit service when the call downstream was not successful" in {
      doNothing().when(mockAuditService).auditStatusCheckEvent(any(), any())(any(), any(), any())
      when(mockConnector.statusPublicFundsByMrz(any())(any(), any()))
        .thenReturn(Future.failed(new Exception("It went wrong")))

      intercept[Exception](await(sut.search(mrzSearchFormModel)))
      verify(mockAuditService, never).auditStatusCheckEvent(any(), any())(any(), any(), any())
    }

  }

}
