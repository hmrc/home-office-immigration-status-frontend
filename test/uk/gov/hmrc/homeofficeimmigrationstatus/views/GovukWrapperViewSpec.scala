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

package uk.gov.hmrc.homeofficeimmigrationstatus.views

import org.joda.time.LocalDate
import org.jsoup.nodes.Document
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.StatusCheckByNinoFormModel
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.MultipleMatchesFoundPage

class GovukWrapperViewSpec extends ViewSpec {

  override implicit lazy val app: Application = new GuiceApplicationBuilder().build()

  lazy val sut = inject[MultipleMatchesFoundPage]

  val query = StatusCheckByNinoFormModel(Nino("AB123456C"), "Pan", "", LocalDate.now().toString)
  lazy val doc: Document = asDocument(sut(query)(request, messages))

  "govuk_wrapper" must {

    "banner contains title" in {
      val docAsString: String = doc.toString()
      docAsString.contains(messages("app.name")) mustBe true
    }

    "banner contains service url" in {
      val docAsString: String = doc.toString()
      docAsString.contains("/check-immigration-status") mustBe true
    }

    "banner contains logo link" in {
      val docAsString: String = doc.toString()
      docAsString.contains("https://www.gov.uk/government/organisations/hm-revenue-customs") mustBe true
    }
  }
}
