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

import models.{MrzSearchFormModel, NinoSearchFormModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers.{redirectLocation, status}
import uk.gov.hmrc.domain.Nino
import utils.NinoGenerator

import java.time.LocalDate
import scala.concurrent.Future

class LandingControllerSpec extends ControllerSpec {

  val sut: LandingController = inject[LandingController]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    super.beforeEach()
  }
  //scalastyle:off magic.number
  val nino: Nino                               = NinoGenerator.generateNino
  val ninoSearchFormModel: NinoSearchFormModel = NinoSearchFormModel(nino, "Pan", "", LocalDate.now())
  val mrzSearchFormModel: MrzSearchFormModel =
    MrzSearchFormModel("PASSPORT", "123456", LocalDate.of(2001, 1, 31), "USA")

  "onPageLoad" must {

    "redirect to nino search with clearForm=true" when {
      "there is a nino query in mongo" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(ninoSearchFormModel)))
        val result = sut.onPageLoad(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.SearchByNinoController.onPageLoad(true).url
      }
    }

    "redirect to mrz search with clearForm=true" when {
      "there is an mrz query in mongo" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(Some(mrzSearchFormModel)))
        val result = sut.onPageLoad(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.SearchByMrzController.onPageLoad(true).url
      }
    }

    "redirect to check by nino with clearForm=false" when {
      "there is no query in mongo" in {
        when(mockSessionCacheService.get(any(), any())).thenReturn(Future.successful(None))
        val result = sut.onPageLoad(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.SearchByNinoController.onPageLoad().url
      }
    }

  }
}
