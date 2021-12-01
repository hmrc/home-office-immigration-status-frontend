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

import controllers.actions.AccessAction
import forms.SearchByNinoForm
import models.{FormQueryModel, NinoSearchFormModel}

import java.time.LocalDate
import org.mockito.ArgumentMatchers.{any, refEq, eq => is}
import org.mockito.Mockito._
import play.api.Application
import play.api.data.FormBinding.Implicits.formBinding
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import utils.NinoGenerator.generateNino
import views.html.SearchByNinoView

import scala.concurrent.Future

class SearchByNinoControllerSpec extends ControllerSpec {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AccessAction].to[FakeAccessAction],
      bind[SearchByNinoView].toInstance(mockView),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  lazy val sut = inject[SearchByNinoController]
  val mockView = mock(classOf[SearchByNinoView])
  val fakeView = HtmlFormat.escape("Correct Form View")

  override def beforeEach(): Unit = {
    reset(mockView)
    when(mockView(any())(any(), any())).thenReturn(fakeView)
    reset(mockSessionCacheService)
    super.beforeEach()
  }

  "onPageLoad" must {
    val query = NinoSearchFormModel(generateNino, "pan", "peter", LocalDate.now())
    val formQuery = FormQueryModel("123", query)
    val emptyForm = inject[SearchByNinoForm].apply()
    val prePopForm = emptyForm.fill(query)

    "display the check by nino form view" when {
      "there is no query on the session" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(None))
        val result = sut.onPageLoad(request)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form was prefilled with a previous query, how?") {
          verify(mockView).apply(refEq(emptyForm, "mapping"))(is(request), any())
        }
        verify(mockSessionCacheService).get(any(), any())
      }

      "there is a existing query on the session" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(formQuery)))
        val result = sut.onPageLoad(request)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form did not prepopulate with the defined query") {
          verify(mockView).apply(refEq(prePopForm, "mapping"))(is(request), any())
        }
        verify(mockSessionCacheService).get(any(), any())
      }

      "the session cache returns a failure" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.failed(new Exception("Something happened")))
        intercept[Exception](await(sut.onPageLoad(request)))
        verify(mockSessionCacheService).get(any(), any())
      }
    }
  }

  "onSubmit" must {
    "redirect to result page" when {
      "form binds correct data" in {
        when(mockSessionCacheService.set(any(), any())(any(), any())).thenReturn(Future.unit)
        val validDob = LocalDate.now().minusDays(1)
        val query = NinoSearchFormModel(generateNino, "pan", "peter", validDob)
        val requestWithForm = request.withFormUrlEncodedBody(
          "dateOfBirth.year"  -> validDob.getYear.toString,
          "dateOfBirth.month" -> validDob.getMonthValue.toString,
          "dateOfBirth.day"   -> validDob.getDayOfMonth.toString,
          "familyName"        -> query.familyName,
          "givenName"         -> query.givenName,
          "nino"              -> query.nino.nino
        )
        val result = sut.onSubmit(requestWithForm)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.StatusResultController.onPageLoad.url
        verify(mockSessionCacheService).set(refEq(query), any())(any(), any())
      }
    }

    "return the errored form" when {
      val form = inject[SearchByNinoForm].apply()
      "the submitted form is empty" in {
        val result = sut.onSubmit(request)
        val formWithErrors = form.bindFromRequest()(request, implicitly)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe fakeView.toString
        verify(mockView).apply(refEq(formWithErrors, "mapping"))(is(request), any())
        withClue("The session should contain the valid form answers") {
          val updatedSession = await(result).session(request)
          updatedSession.get("query") must not be defined
        }
        verify(mockSessionCacheService, never).set(any(), any())(any(), any())
      }

      "the form has errors" in {
        val requestWithForm = request.withFormUrlEncodedBody(
          "dateOfBirth.year"  -> "blah",
          "dateOfBirth.month" -> "blah",
          "dateOfBirth.day"   -> "blah",
          "familyName"        -> "blah",
          "givenName"         -> "blah",
          "nino"              -> "blah"
        )
        val result = sut.onSubmit(requestWithForm)
        val formWithErrors = form.bindFromRequest()(requestWithForm, implicitly)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe fakeView.toString
        verify(mockView).apply(refEq(formWithErrors, "mapping"))(is(requestWithForm), any())
        withClue("The session should contain the valid form answers") {
          val updatedSession = await(result).session(request)
          updatedSession.get("query") must not be defined
        }
        verify(mockSessionCacheService, never).set(any(), any())(any(), any())
      }

      "the session cache returns a failure" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.failed(new Exception("Something happened")))
        val validDob = LocalDate.now().minusDays(1)
        val query = NinoSearchFormModel(generateNino, "pan", "peter", validDob)
        val requestWithForm = request.withFormUrlEncodedBody(
          "dateOfBirth.year"  -> validDob.getYear.toString,
          "dateOfBirth.month" -> validDob.getMonthValue.toString,
          "dateOfBirth.day"   -> validDob.getDayOfMonth.toString,
          "familyName"        -> query.familyName,
          "givenName"         -> query.givenName,
          "nino"              -> query.nino.nino
        )
        intercept[Exception](await(sut.onSubmit(requestWithForm)))
      }
    }
  }
}
