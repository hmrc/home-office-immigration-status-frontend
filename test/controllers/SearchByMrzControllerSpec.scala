/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.SearchByMRZForm
import models.MrzSearchFormModel
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import play.api.Application
import play.api.data.FormBinding.Implicits.formBinding
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import play.twirl.api.{Html, HtmlFormat}
import services.SessionCacheService
import views.html.SearchByMrzView

import java.time.LocalDate
import scala.concurrent.Future

class SearchByMrzControllerSpec extends ControllerSpec {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AccessAction].to[FakeAccessAction],
      bind[SearchByMrzView].toInstance(mockView),
      bind[SessionCacheService].toInstance(mockSessionCacheService)
    )
    .build()

  lazy val sut: SearchByMrzController = inject[SearchByMrzController]
  val mockView: SearchByMrzView       = mock(classOf[SearchByMrzView])
  val fakeView: Html                  = HtmlFormat.escape("Correct Form View")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockView)
    when(mockView(any())(any(), any())).thenReturn(fakeView)
    reset(mockSessionCacheService)
  }

  "onPageLoad" must {
    val query      = MrzSearchFormModel("docType", "docNum", LocalDate.now(), "nationality")
    val emptyForm  = inject[SearchByMRZForm].apply()
    val prePopForm = emptyForm.fill(query)

    "display the search by mrz form view" when {
      "there is no query on the session" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(None))
        val result = sut.onPageLoad(false)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form was prefilled with a previous query, how?") {
          verify(mockView).apply(refEq(emptyForm, "mapping"))(ArgumentMatchers.eq(request), any())
        }
        verify(mockSessionCacheService).get(any(), any())
      }

      "there is a existing query on the session" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(query)))

        val result = sut.onPageLoad(false)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form did not prepopulate with the defined query") {
          verify(mockView).apply(refEq(prePopForm, "mapping"))(ArgumentMatchers.eq(request), any())
        }
        verify(mockSessionCacheService).get(any(), any())
      }

      "the session cache returns a failure" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.failed(new Exception("Something happened")))
        intercept[Exception](await(sut.onPageLoad(false)(request)))
        verify(mockSessionCacheService).get(any(), any())
      }
    }

    "clear mongo and display an empty form" when {
      "clearForm is set to true" in {
        when(mockSessionCacheService.delete(any(), any())).thenReturn(Future.unit)
        val result = sut.onPageLoad(true)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form was prefilled with a previous query, how?") {
          verify(mockView).apply(refEq(emptyForm, "mapping"))(ArgumentMatchers.eq(request), any())
        }
        verify(mockSessionCacheService).delete(any(), any())
      }
    }
  }

  "onSubmit" must {
    "redirect to result page" when {
      "form binds correct data" in {
        when(mockSessionCacheService.set(any())(any(), any())).thenReturn(Future.unit)
        val validDob = LocalDate.now().minusDays(1)
        val query    = MrzSearchFormModel("PASSPORT", "1234567890", validDob, "AFG")

        val requestWithForm = fakePostRequest.withFormUrlEncodedBody(
          "dateOfBirth.year"  -> validDob.getYear.toString,
          "dateOfBirth.month" -> validDob.getMonthValue.toString,
          "dateOfBirth.day"   -> validDob.getDayOfMonth.toString,
          "nationality"       -> query.nationality,
          "documentType"      -> query.documentType,
          "documentNumber"    -> query.documentNumber
        )
        val result = sut.onSubmit(requestWithForm)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.StatusResultController.onPageLoad.url
        verify(mockSessionCacheService).set(ArgumentMatchers.eq(query))(any(), any())
      }
    }

    "return the errored form" when {
      val formProvider = inject[SearchByMRZForm]
      val form         = formProvider.apply()
      "the submitted form is empty" in {
        val result         = sut.onSubmit(request)
        val formWithErrors = formProvider.collateDOBErrors(form.bindFromRequest()(request, implicitly))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe fakeView.toString
        verify(mockView).apply(refEq(formWithErrors, "mapping"))(ArgumentMatchers.eq(request), any())
        withClue("The session should contain the valid form answers") {
          val updatedSession = await(result).session(request)
          updatedSession.get("query") must not be defined
        }
        verify(mockSessionCacheService, never).set(any())(any(), any())
      }

      "the form has errors" in {
        val requestWithForm = fakePostRequest.withFormUrlEncodedBody(
          "dateOfBirth.year"  -> "blah",
          "dateOfBirth.month" -> "blah",
          "dateOfBirth.day"   -> "blah",
          "nationality"       -> "blah",
          "documentType"      -> "blah",
          "documentNumber"    -> "blah"
        )
        val result         = sut.onSubmit(requestWithForm)
        val formWithErrors = formProvider.collateDOBErrors(form.bindFromRequest()(requestWithForm, implicitly))

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe fakeView.toString
        verify(mockView).apply(refEq(formWithErrors, "mapping"))(ArgumentMatchers.eq(requestWithForm), any())
        withClue("The session should contain the valid form answers") {
          val updatedSession = await(result).session(request)
          updatedSession.get("query") must not be defined
        }
        verify(mockSessionCacheService, never).set(any())(any(), any())
      }

      "the session cache returns a failure" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.failed(new Exception("Something happened")))
        val validDob = LocalDate.now().minusDays(1)
        val query    = MrzSearchFormModel("PASSPORT", "1234567890", validDob, "AFG")
        val requestWithForm = fakePostRequest.withFormUrlEncodedBody(
          "dateOfBirth.year"  -> validDob.getYear.toString,
          "dateOfBirth.month" -> validDob.getMonthValue.toString,
          "dateOfBirth.day"   -> validDob.getDayOfMonth.toString,
          "nationality"       -> query.nationality,
          "documentType"      -> query.documentType,
          "documentNumber"    -> query.documentNumber
        )
        intercept[Exception](await(sut.onSubmit(requestWithForm)))
      }
    }

  }

}
