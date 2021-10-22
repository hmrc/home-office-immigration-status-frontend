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

package uk.gov.hmrc.homeofficeimmigrationstatus.controllers

import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.{any, refEq, eq => is}
import org.mockito.Mockito.{mock, reset, spy, verify, when}
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsString, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.actions.AuthAction
import uk.gov.hmrc.homeofficeimmigrationstatus.forms.StatusCheckByNinoFormProvider
import uk.gov.hmrc.homeofficeimmigrationstatus.models.StatusCheckByNinoFormModel
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.StatusCheckByNinoPage

class StatusCheckByNinoControllerSpec extends ControllerSpec {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to[FakeAuthAction],
      bind[StatusCheckByNinoPage].toInstance(mockView)
    )
    .build()

  lazy val sut = inject[StatusCheckByNinoController]
  val mockView = mock(classOf[StatusCheckByNinoPage])
  val fakeView = HtmlFormat.escape("Correct Form View")

  override def beforeEach(): Unit = {
    reset(mockView)
    when(mockView(any(), any())(any(), any(), any())).thenReturn(fakeView)
    super.beforeEach()
  }

  "onPageLoad" must {
    //TODO NINO GEN
    val query = StatusCheckByNinoFormModel(Nino("AB123456C"), "pan", "peter", LocalDate.now().toString)
    val emptyForm = inject[StatusCheckByNinoFormProvider].apply()
    val prePopForm = emptyForm.fill(query)

    "display the check by nino form view" when {
      "there is no query on the session" in {
        val result = sut.onPageLoad(request)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form was prefilled with a previous query, how?") {
          verify(mockView).apply(refEq(emptyForm, "mapping"), any())(is(request), any(), any())
        }
      }

      "there is a existing query on the session" in {
        val requestWithQuery = request.withSession("query" -> Json.toJson(query).toString)
        val result = sut.onPageLoad(requestWithQuery)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form did not prepopulate with the defined query") {
          verify(mockView).apply(refEq(prePopForm, "mapping"), any())(is(requestWithQuery), any(), any())
        }
      }

      "the query can not be read on the session" in {
        val requestWithQuery = request.withSession("query" -> "unreadable query")
        val result = sut.onPageLoad(requestWithQuery)

        status(result) mustBe OK
        contentAsString(result) mustBe fakeView.toString
        withClue("the form was prefilled with a previous query, how?") {
          verify(mockView).apply(refEq(emptyForm, "mapping"), any())(is(requestWithQuery), any(), any())
        }
      }
    }
  }

  "onSubmit" must {
    "redirect to result page" when {
      "form binds correct data" in {
        val result = sut.onSubmit(request)

        status(result) mustBe OK

        withClue("The session should contain the valid form answers"){

        }
      }
    }
    "return the errored form" when {
      "the form does not bind" in {

      }
    }
  }

}
