/*
 * Copyright 2024 HM Revenue & Customs
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

package views.components

import java.time.LocalDate
import models._
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.NinoGenerator.generateNino
import views._
import views.html.components.StatusFoundWarnings

class StatusFoundWarningsSpec extends ViewSpec {

  private val ninoSearchFormModel: NinoSearchFormModel = NinoSearchFormModel(
    nino = generateNino,
    givenName = "Josh",
    familyName = "Walker",
    dateOfBirth = LocalDate.parse("1990-02-01")
  )

  private val immigrationStatus: ImmigrationStatus = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2016-11-08"),
    statusEndDate = Some(LocalDate.parse("2030-06-17")),
    productType = "EUS",
    immigrationStatus = "LTR",
    noRecourseToPublicFunds = true
  )

  private val statusCheckResult: StatusCheckResult = StatusCheckResult(
    fullName = "Josh Walker",
    dateOfBirth = LocalDate.parse("1990-02-01"),
    nationality = "JPN",
    statuses = List(immigrationStatus)
  )

  private val statusFoundPageContext: StatusFoundPageContext = StatusFoundPageContext(
    query = ninoSearchFormModel,
    result = statusCheckResult
  )

  private val sut: StatusFoundWarnings = inject[StatusFoundWarnings]

  private val viewViaApply: HtmlFormat.Appendable  = sut.apply(statusFoundPageContext)(messages)
  private val viewViaRender: HtmlFormat.Appendable = sut.render(statusFoundPageContext, messages)
  private val viewViaF: HtmlFormat.Appendable      = sut.f(statusFoundPageContext)(messages)

  "StatusFoundWarnings" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" must {
        val doc: Document = asDocument(view)
        "have the zambrano warning message" in {
          val message: String = "! Warning This is a rest of the world national with an EU Settlement Scheme status. " +
            "Eligibility needs to be checked on Home Office systems (such as ATLAS)."

          assertElementHasText(doc, "#zambrano-warning", message)
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => test.tupled(args))
  }
}
