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

package services

import config.AppConfig
import connectors.HomeOfficeImmigrationStatusProxyConnector
import controllers.ControllerSpec
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.SessionCacheRepository
import utils.NinoGenerator

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HomeOfficeImmigrationStatusProxyServiceSpec extends ControllerSpec {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy")

  val mockAuditService: AuditService = mock(classOf[AuditService])
  val mockConnector: HomeOfficeImmigrationStatusProxyConnector = mock(
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
    .build()

  lazy val sut: HomeOfficeImmigrationStatusProxyService =
    app.injector.instanceOf[HomeOfficeImmigrationStatusProxyService]
  //scalastyle:off magic.number
  val testDate: LocalDate = LocalDate.now
  val formModel: NinoSearchFormModel =
    NinoSearchFormModel(NinoGenerator.generateNino, "Doe", "Jane", LocalDate.of(2001, 1, 31))
  val mrzSearchFormModel: MrzSearchFormModel =
    MrzSearchFormModel("PASSPORT", "123456", LocalDate.of(2001, 1, 31), "USA")
  val statusRequest: Search    = formModel.toSearch(6)
  implicit val conf: AppConfig = appConfig

  val statusCheckResult: StatusCheckResult = StatusCheckResult("Damon Albarn", testDate, "GBR", Nil)
  val result: StatusCheckResponseWithStatus =
    StatusCheckResponseWithStatus(200, StatusCheckSuccessfulResponse(Some("CorrelationId"), statusCheckResult))

  "statusPublicFundsByNino" should {
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

  "statusPublicFundsByMrz" should {
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
