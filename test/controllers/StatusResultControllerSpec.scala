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

import services.HomeOfficeImmigrationStatusProxyService
import controllers.actions.AccessAction
import models._
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import services.SessionCacheService
import uk.gov.hmrc.domain.Nino
import views.html.{ExternalErrorPage, MultipleMatchesFoundPage, StatusCheckFailurePage, StatusFoundPage, StatusNotAvailablePage}
import views.{StatusFoundPageContext, StatusNotAvailablePageContext}
import models.HomeOfficeError._

import scala.concurrent.Future

class StatusResultControllerSpec extends ControllerSpec {

  lazy val sut = inject[StatusResultController]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AccessAction].to[FakeAccessAction],
      bind[HomeOfficeImmigrationStatusProxyService].toInstance(mockProxyService),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  override def beforeEach(): Unit = {
    reset(mockProxyService)
    reset(mockSessionCacheService)
    super.beforeEach()
  }

  val mockProxyService = mock(classOf[HomeOfficeImmigrationStatusProxyService])

  "onPageLoad" must {
    "redirect to the form" when {
      "there is no query to search" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(None))
        val result = sut.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.StatusCheckByNinoController.onPageLoad.url
        withClue("Connector should not be called") {
          verify(mockProxyService, times(0)).statusPublicFundsByNino(any())(any(), any(), any(), any())
        }
        verify(mockSessionCacheService).get(any(), any())
      }
    }

    "display the return from HO" when {
      val query = StatusCheckByNinoFormModel(Nino("AB123456C"), "pan", "peter", LocalDate.now().toString)
      val formQuery = FormQueryModel("123", query)

      def mockProxyServiceWith(hoResponse: Either[HomeOfficeError, StatusCheckResponse]) =
        when(
          mockProxyService
            .statusPublicFundsByNino(refEq(query))(any(), any(), any(), any()))
          .thenReturn(Future.successful(hoResponse))

      def verifyConnector() = verify(mockProxyService).statusPublicFundsByNino(any())(any(), any(), any(), any())

      "is found with statuses" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        val hoResult = StatusCheckResult(
          "",
          java.time.LocalDate.now(),
          "",
          List(ImmigrationStatus(java.time.LocalDate.now(), None, "", "", false)))
        mockProxyServiceWith(Right(StatusCheckResponse("id", result = hoResult)))

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
        mockProxyServiceWith(Right(StatusCheckResponse("id", result = hoResult)))

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
        mockProxyServiceWith(Left(StatusCheckConflict("Some response")))

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[MultipleMatchesFoundPage]
          .apply(query)(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "has not found error" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        mockProxyServiceWith(Left(StatusCheckNotFound("Some response")))

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[StatusCheckFailurePage]
          .apply(query)(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "has some other error" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        val TEAPOT = 418
        mockProxyServiceWith(Left(OtherErrorResponse(TEAPOT, "Some response")))

        val result = sut.onPageLoad()(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) mustBe inject[ExternalErrorPage]
          .apply()(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }
    }
  }
}
