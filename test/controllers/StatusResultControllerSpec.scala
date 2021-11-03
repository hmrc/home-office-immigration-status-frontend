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

package controllers

import connectors.HomeOfficeImmigrationStatusProxyConnector
import controllers.actions.AuthAction
import models._
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import services.SessionCacheService
import uk.gov.hmrc.domain.Nino
import views.html.{MultipleMatchesFoundPage, StatusCheckFailurePage, StatusFoundPage, StatusNotAvailablePage}
import views.{StatusFoundPageContext, StatusNotAvailablePageContext}

import scala.concurrent.Future

class StatusResultControllerSpec extends ControllerSpec {

  lazy val sut = inject[StatusResultController]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to[FakeAuthAction],
      bind[HomeOfficeImmigrationStatusProxyConnector].toInstance(mockConnector),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  override def beforeEach(): Unit = {
    reset(mockConnector)
    reset(mockSessionCacheService)
    super.beforeEach()
  }

  val mockConnector = mock(classOf[HomeOfficeImmigrationStatusProxyConnector])

  "onPageLoad" must {
    "redirect to the form" when {
      "there is no query to search" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(None))
        val result = sut.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.StatusCheckByNinoController.onPageLoad.url
        withClue("Connector should not be called") {
          verify(mockConnector, times(0)).statusPublicFundsByNino(any())(any(), any())
        }
        verify(mockSessionCacheService).get(any(), any())
      }
    }

    "display the return from HO" when {
      val query = StatusCheckByNinoFormModel(Nino("AB123456C"), "pan", "peter", LocalDate.now().toString)
      val formQuery = FormQueryModel("123", query)

      def mockConnectorWith(hoResponse: StatusCheckResponse) =
        when(
          mockConnector
            .statusPublicFundsByNino(refEq(query.toRequest(appConfig.defaultQueryTimeRangeInMonths)))(any(), any()))
          .thenReturn(Future.successful(hoResponse))

      def verifyConnector() = verify(mockConnector).statusPublicFundsByNino(any())(any(), any())

      "is found with statuses" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        val hoResult = StatusCheckResult(
          "",
          java.time.LocalDate.now(),
          "",
          List(ImmigrationStatus(java.time.LocalDate.now(), None, "", "", false)))
        mockConnectorWith(StatusCheckResponse("id", result = Some(hoResult)))

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[StatusFoundPage]
          .apply(StatusFoundPageContext(query, hoResult, routes.LandingController.onPageLoad))(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "is found with no statuses" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        val hoResult = StatusCheckResult("", java.time.LocalDate.now(), "", Nil)
        mockConnectorWith(StatusCheckResponse("id", result = Some(hoResult)))

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[StatusNotAvailablePage]
          .apply(StatusNotAvailablePageContext(query, routes.LandingController.onPageLoad))(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "has conflict error" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        val hoError = StatusCheckError("ERR_CONFLICT")
        mockConnectorWith(StatusCheckResponse("id", error = Some(hoError)))

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[MultipleMatchesFoundPage]
          .apply(query)(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "has some other error" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        val hoError = StatusCheckError("OTHER")
        mockConnectorWith(StatusCheckResponse("id", error = Some(hoError)))

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[StatusCheckFailurePage]
          .apply(query)(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }
    }
  }
}