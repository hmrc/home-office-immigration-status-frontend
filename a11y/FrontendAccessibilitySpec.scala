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

import java.time.LocalDate

import forms._
import models._
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.scalatestaccessibilitylinter.views.AutomaticAccessibilitySpec
import utils.NinoGenerator.generateNino
import views.html._
import views._

class FrontendAccessibilitySpec extends AutomaticAccessibilitySpec {

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

  private val statusNotAvailablePageContext: StatusNotAvailablePageContext = StatusNotAvailablePageContext(
    query = ninoSearchFormModel,
    result = statusCheckResult
  )

  private val searchByNinoForm: SearchByNinoForm = new SearchByNinoForm()
  private val searchByMrzForm: SearchByMRZForm   = app.injector.instanceOf[SearchByMRZForm]

  override implicit val arbAsciiString: Arbitrary[String]                                 = fixed("/")
  implicit val arbSearchByNinoForm: Arbitrary[Form[NinoSearchFormModel]]                  = fixed(searchByNinoForm())
  implicit val arbSearchByMrzForm: Arbitrary[Form[MrzSearchFormModel]]                    = fixed(searchByMrzForm())
  implicit val arbSearchFormModel: Arbitrary[SearchFormModel]                             = fixed(ninoSearchFormModel)
  implicit val arbStatusFoundPageContext: Arbitrary[StatusFoundPageContext]               = fixed(statusFoundPageContext)
  implicit val arbStatusNotAvailablePageContext: Arbitrary[StatusNotAvailablePageContext] = fixed(statusNotAvailablePageContext)

  override def viewPackageName: String = "views.html"

  override def layoutClasses: Seq[Class[govuk_wrapper]] = Seq(classOf[govuk_wrapper])

  override def renderViewByClass: PartialFunction[Any, Html] = {
    case accessibilityStatementPage: AccessibilityStatementPage => render(accessibilityStatementPage)
    case externalErrorPage: ExternalErrorPage                   => render(externalErrorPage)
    case searchByMrzView: SearchByMrzView                       => render(searchByMrzView)
    case searchByNinoView: SearchByNinoView                     => render(searchByNinoView)
    case shutteringPage: ShutteringPage                         => render(shutteringPage)
    case statusCheckFailurePage: StatusCheckFailurePage         => render(statusCheckFailurePage)
    case statusFoundPage: StatusFoundPage                       => render(statusFoundPage)
    case statusNotAvailablePage: StatusNotAvailablePage         => render(statusNotAvailablePage)
    case errorTemplate: error_template                          => render(errorTemplate)
  }

  runAccessibilityTests()
}
