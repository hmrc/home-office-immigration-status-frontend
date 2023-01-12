/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.actions.AccessAction
import models._
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import play.api.Application
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import repositories.SessionCacheRepository
import services.{HomeOfficeImmigrationStatusProxyService, SessionCacheService}
import utils.NinoGenerator.generateNino
import views.html._
import views.{StatusFoundPageContext, StatusNotAvailablePageContext}

import java.time.LocalDate
import scala.concurrent.Future

class StatusResultControllerSpec extends ControllerSpec {

  lazy val sut: StatusResultController = inject[StatusResultController]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AccessAction].to[FakeAccessAction],
      bind[HomeOfficeImmigrationStatusProxyService].toInstance(mockProxyService),
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  override def beforeEach(): Unit = {
    reset(mockProxyService)
    reset(mockSessionCacheService)
    super.beforeEach()
  }

  val mockProxyService: HomeOfficeImmigrationStatusProxyService = mock(classOf[HomeOfficeImmigrationStatusProxyService])

  val correlationId: Some[String] = Some("CorrelationId")

  "onPageLoad" must {
    "redirect to the form" when {
      "there is no query to search" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(None))
        val result = sut.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.SearchByNinoController.onPageLoad().url
        withClue("Connector should not be called") {
          verify(mockProxyService, times(0)).search(any())(any(), any(), any(), any())
        }
        verify(mockSessionCacheService).get(any(), any())
      }
    }

    "display the return from HO" when {
      val query = NinoSearchFormModel(generateNino, "pan", "peter", LocalDate.now())

      def mockProxyServiceWith(
        hoResponse: StatusCheckResponseWithStatus
      ): OngoingStubbing[Future[StatusCheckResponseWithStatus]] =
        when(
          mockProxyService
            .search(refEq(query))(any(), any(), any(), any())
        )
          .thenReturn(Future.successful(hoResponse))

      def verifyConnector(): Future[StatusCheckResponseWithStatus] =
        verify(mockProxyService).search(any())(any(), any(), any(), any())

      "is found with statuses" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(query)))
        val hoResult = StatusCheckResult(
          "",
          java.time.LocalDate.now(),
          "",
          List(ImmigrationStatus(java.time.LocalDate.now(), None, "", "", noRecourseToPublicFunds = false))
        )
        mockProxyServiceWith(
          StatusCheckResponseWithStatus(OK, StatusCheckSuccessfulResponse(correlationId, result = hoResult))
        )

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[StatusFoundPage]
          .apply(StatusFoundPageContext(query, hoResult))(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "is found with no statuses" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(query)))
        val hoResult = StatusCheckResult("", java.time.LocalDate.now(), "", Nil)
        mockProxyServiceWith(
          StatusCheckResponseWithStatus(OK, StatusCheckSuccessfulResponse(correlationId, result = hoResult))
        )

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[StatusNotAvailablePage]
          .apply(StatusNotAvailablePageContext(query, hoResult))(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "has conflict error" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(query)))
        mockProxyServiceWith(
          StatusCheckResponseWithStatus(
            CONFLICT,
            StatusCheckErrorResponse(correlationId, StatusCheckError("Some response"))
          )
        )

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[MultipleMatchesFoundPage]
          .apply(query)(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "has not found error" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(query)))
        mockProxyServiceWith(
          StatusCheckResponseWithStatus(
            NOT_FOUND,
            StatusCheckErrorResponse(correlationId, StatusCheckError("Some response"))
          )
        )

        val result = sut.onPageLoad()(request)

        status(result) mustBe OK
        contentAsString(result) mustBe inject[StatusCheckFailurePage]
          .apply(query)(request, messages)
          .toString
        verifyConnector()
        verify(mockSessionCacheService).get(any(), any())
      }

      "has some other error" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(query)))
        val TEAPOT = 418
        mockProxyServiceWith(
          StatusCheckResponseWithStatus(
            TEAPOT,
            StatusCheckErrorResponse(correlationId, StatusCheckError("Some response"))
          )
        )

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
