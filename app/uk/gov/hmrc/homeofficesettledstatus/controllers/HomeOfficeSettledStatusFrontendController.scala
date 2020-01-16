/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatus.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.connectors.{FrontendAuthConnector, HomeOfficeSettledStatusProxyConnector}
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State._
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyService
import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckRange}
import uk.gov.hmrc.homeofficesettledstatus.views.html.{main_template, start_page}
import uk.gov.hmrc.homeofficesettledstatus.wiring.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.fsm.JourneyController
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF, Input}

import scala.concurrent.ExecutionContext

@Singleton
class HomeOfficeSettledStatusFrontendController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  homeOfficeSettledStatusProxyConnector: HomeOfficeSettledStatusProxyConnector,
  val authConnector: FrontendAuthConnector,
  val env: Environment,
  input: Input,
  form: FormWithCSRF,
  errorSummary: ErrorSummary,
  mainTemplate: main_template,
  override val journeyService: HomeOfficeSettledStatusFrontendJourneyService,
  controllerComponents: MessagesControllerComponents)(
  implicit val config: Configuration,
  ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthActions
    with JourneyController[HeaderCarrier] {

  import HomeOfficeSettledStatusFrontendController._
  import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel._

  override implicit def context(implicit rh: RequestHeader): HeaderCarrier = hc

  val AsStrideUser: WithAuthorised[String] = { implicit request =>
    authorisedWithStrideGroup(appConfig.authorisedStrideGroup)
  }

  // GET /
  val showStart: Action[AnyContent] = actionShowState {
    case Start =>
  }

  // GET /status-check-nino
  val showStatusCheckByNino: Action[AnyContent] =
    actionShowStateWhenAuthorised(AsStrideUser) {
      case StatusCheckByNino =>
    }

  // POST /status-check-nino
  val confirmStatusCheckByNino: Action[AnyContent] = action { implicit request =>
    whenAuthorisedWithForm(AsStrideUser)(StatusCheckByNinoRequestForm)(
      Transitions.confirmStatusCheckByNino)
  }

  // GET /status-check-nino/confirm
  val showConfirmStatusCheckByNino: Action[AnyContent] =
    actionShowStateWhenAuthorised(AsStrideUser) {
      case _: ConfirmStatusCheckByNino =>
    }

  // POST /status-check-nino/confirm
  val submitStatusCheckByNino: Action[AnyContent] = action { implicit request =>
    whenAuthorised(AsStrideUser)(
      Transitions.submitStatusCheckByNino(
        homeOfficeSettledStatusProxyConnector.statusPublicFundsByNino(_)))(redirect)
  }

  // GET /status-found
  val showStatusFound: Action[AnyContent] =
    actionShowStateWhenAuthorised(AsStrideUser) {
      case _: StatusFound =>
    }

  // GET /status-check-failure
  val showStatusCheckFailure: Action[AnyContent] =
    actionShowStateWhenAuthorised(AsStrideUser) {
      case _: StatusCheckFailure =>
    }

  /**
    * Function from the `State` to the `Call` (route),
    * used by play-fsm internally to create redirects.
    */
  override def getCallFor(state: State)(implicit request: Request[_]): Call = state match {
    case Start => routes.HomeOfficeSettledStatusFrontendController.showStart()
    case StatusCheckByNino =>
      routes.HomeOfficeSettledStatusFrontendController.showStatusCheckByNino()
    case _: ConfirmStatusCheckByNino =>
      routes.HomeOfficeSettledStatusFrontendController.showConfirmStatusCheckByNino()
    case _: StatusFound        => routes.HomeOfficeSettledStatusFrontendController.showStatusFound()
    case _: StatusCheckFailure => routes.HomeOfficeSettledStatusFrontendController.showStart()
  }

  /**
    * Function from the `State` to the `Result`,
    * used by play-fsm internally to render the actual content.
    */
  override def renderState(state: State, breadcrumbs: List[State], formWithErrors: Option[Form[_]])(
    implicit request: Request[_]): Result = state match {

    case Start =>
      Ok(new start_page(mainTemplate, input, form, errorSummary)())

    case _ =>
      NotImplemented("Not yet implemented, sorry!")
  }
}

object HomeOfficeSettledStatusFrontendController {

  import FormFieldMappings._

  val StatusCheckByNinoRequestForm = Form[StatusCheckByNinoRequest](
    mapping(
      "dateOfBirth" -> dateOfBirthMapping,
      "familyName"  -> trimmedUppercaseText,
      "givenName"   -> trimmedUppercaseText,
      "nino" -> uppercaseNormalizedText
        .verifying(validNino())
        .transform(Nino.apply, (n: Nino) => n.toString),
      "range" -> ignored[Option[StatusCheckRange]](None)
    )(StatusCheckByNinoRequest.apply)(StatusCheckByNinoRequest.unapply))
}
