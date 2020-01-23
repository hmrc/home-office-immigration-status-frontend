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
import uk.gov.hmrc.homeofficesettledstatus.views.LayoutComponents
import uk.gov.hmrc.homeofficesettledstatus.views.html.{StatusCheckByNinoPage, StatusFoundPage}
import uk.gov.hmrc.homeofficesettledstatus.wiring.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.fsm.{JourneyController, JourneyIdSupport}

import scala.concurrent.ExecutionContext
import scala.util.Success

@Singleton
class HomeOfficeSettledStatusFrontendController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  homeOfficeSettledStatusProxyConnector: HomeOfficeSettledStatusProxyConnector,
  val authConnector: FrontendAuthConnector,
  val env: Environment,
  override val journeyService: HomeOfficeSettledStatusFrontendJourneyService,
  controllerComponents: MessagesControllerComponents,
  layoutComponents: LayoutComponents)(implicit val config: Configuration, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthActions
    with JourneyController[HeaderCarrier] with JourneyIdSupport[HeaderCarrier] {

  import HomeOfficeSettledStatusFrontendController._
  import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel._

  val AsHuman: WithAuthorised[String] = { implicit request =>
    withAuthorisedAsHuman
  }

  val AsStrideUser: WithAuthorised[String] = { implicit request =>
    authorisedWithStrideGroup(appConfig.authorisedStrideGroup)
  }

  // GET /
  val showStart: Action[AnyContent] =
    action { implicit request =>
      whenAuthorised(AsStrideUser)(Transitions.start)(display)
        .andThen {
          // reset navigation history
          case Success(_) => journeyService.cleanBreadcrumbs()
        }
    }

  // GET /check-with-nino
  val showStatusCheckByNino: Action[AnyContent] =
    actionShowStateWhenAuthorised(AsStrideUser) {
      case StatusCheckByNino =>
    }

  // POST /check-with-nino
  val submitStatusCheckByNino: Action[AnyContent] =
    action { implicit request =>
      whenAuthorisedWithForm(AsStrideUser)(StatusCheckByNinoRequestForm)(
        Transitions.submitStatusCheckByNino(
          homeOfficeSettledStatusProxyConnector.statusPublicFundsByNino(_))
      )
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
    case _: StatusFound => routes.HomeOfficeSettledStatusFrontendController.showStatusFound()
    case _: StatusCheckFailure =>
      routes.HomeOfficeSettledStatusFrontendController.showStatusCheckFailure()
  }

  import uk.gov.hmrc.play.fsm.OptionalFormOps._

  val statusCheckByNinoPage = new StatusCheckByNinoPage(layoutComponents)
  val statusFoundPage = new StatusFoundPage(layoutComponents)

  /**
    * Function from the `State` to the `Result`,
    * used by play-fsm internally to render the actual content.
    */
  override def renderState(state: State, breadcrumbs: List[State], formWithErrors: Option[Form[_]])(
    implicit request: Request[_]): Result = state match {

    case Start =>
      Redirect(getCallFor(StatusCheckByNino))

    case StatusCheckByNino =>
      Ok(
        statusCheckByNinoPage(
          formWithErrors.or(StatusCheckByNinoRequestForm),
          routes.HomeOfficeSettledStatusFrontendController.submitStatusCheckByNino()))

    case StatusFound(correlationId, query, result) => Ok(statusFoundPage(query, result))

    case _ =>
      NotImplemented("Not yet implemented, sorry!")
  }

  override implicit def context(implicit rh: RequestHeader): HeaderCarrier =
    appendJourneyId(super.hc)

  override def amendContext(
    headerCarrier: HeaderCarrier)(key: String, value: String): HeaderCarrier =
    headerCarrier.withExtraHeaders(key -> value)
}

object HomeOfficeSettledStatusFrontendController {

  import FormFieldMappings._

  val StatusCheckByNinoRequestForm = Form[StatusCheckByNinoRequest](
    mapping(
      "dateOfBirth" -> dateOfBirthMapping,
      "familyName"  -> trimmedUppercaseName.verifying(validName("familyName", 3)),
      "givenName"   -> trimmedUppercaseName.verifying(validName("givenName", 1)),
      "nino" -> uppercaseNormalizedText
        .verifying(validNino())
        .transform(Nino.apply, (n: Nino) => n.toString),
      "range" -> ignored[Option[StatusCheckRange]](None)
    )(StatusCheckByNinoRequest.apply)(StatusCheckByNinoRequest.unapply))
}
