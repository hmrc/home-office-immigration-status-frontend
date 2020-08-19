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
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State.{StatusCheckFailure, _}
import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckRange}
import uk.gov.hmrc.homeofficesettledstatus.services.HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier
import uk.gov.hmrc.homeofficesettledstatus.views.html.{MultipleMatchesFoundPage, StatusCheckByNinoPage, StatusCheckFailurePage, StatusFoundPage, StatusNotAvailablePage}
import uk.gov.hmrc.homeofficesettledstatus.views.{LayoutComponents, StatusFoundPageContext, StatusNotAvailablePageContext}
import uk.gov.hmrc.homeofficesettledstatus.wiring.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
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
  override val journeyService: HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier,
  controllerComponents: MessagesControllerComponents,
  layoutComponents: LayoutComponents)(implicit val config: Configuration, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthActions
    with JourneyController[HeaderCarrier] with JourneyIdSupport[HeaderCarrier] {

  import HomeOfficeSettledStatusFrontendController._
  import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel._

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
    action { implicit request =>
      whenAuthorised(AsStrideUser)(Transitions.showStatusCheckByNino)(display)
        .andThen {
          // reset navigation history
          case Success(_) => journeyService.cleanBreadcrumbs()
        }
    }

  // POST /check-with-nino
  val submitStatusCheckByNino: Action[AnyContent] =
    action { implicit request =>
      whenAuthorisedWithForm(AsStrideUser)(StatusCheckByNinoRequestForm)(
        Transitions.submitStatusCheckByNino(
          homeOfficeSettledStatusProxyConnector.statusPublicFundsByNino(_),
          appConfig.defaultQueryTimeRangeInMonths)
      )
    }

  // GET /status-found
  val showStatusFound: Action[AnyContent] =
    actionShowStateWhenAuthorised(AsStrideUser) {
      case _: StatusFound =>
    }

  // GET /status-not-available
  val showStatusNotAvailable: Action[AnyContent] =
    actionShowStateWhenAuthorised(AsStrideUser) {
      case _: StatusNotAvailable =>
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
    case Start =>
      routes.HomeOfficeSettledStatusFrontendController.showStart()
    case _: StatusCheckByNino =>
      routes.HomeOfficeSettledStatusFrontendController.showStatusCheckByNino()
    case _: StatusNotAvailable =>
      routes.HomeOfficeSettledStatusFrontendController.showStatusNotAvailable()
    case _: StatusFound =>
      routes.HomeOfficeSettledStatusFrontendController.showStatusFound()
    case _: StatusCheckFailure =>
      routes.HomeOfficeSettledStatusFrontendController.showStatusCheckFailure()
  }

  import uk.gov.hmrc.play.fsm.OptionalFormOps._

  val statusCheckByNinoPage = new StatusCheckByNinoPage(layoutComponents)
  val statusFoundPage = new StatusFoundPage(layoutComponents)
  val statusNotAvailablePage = new StatusNotAvailablePage(layoutComponents)
  val statusCheckFailurePage = new StatusCheckFailurePage(layoutComponents)
  val multipleMatchesFoundPage = new MultipleMatchesFoundPage(layoutComponents)

  /**
    * Function from the `State` to the `Result`,
    * used by play-fsm internally to render the actual content.
    */
  override def renderState(state: State, breadcrumbs: List[State], formWithErrors: Option[Form[_]])(
    implicit request: Request[_]): Result = state match {

    case Start =>
      Redirect(getCallFor(StatusCheckByNino()))

    case StatusCheckByNino(maybeQuery) =>
      Ok(
        statusCheckByNinoPage(
          formWithErrors.or(
            maybeQuery
              .map(query => StatusCheckByNinoRequestForm.fill(query))
              .getOrElse(StatusCheckByNinoRequestForm)),
          routes.HomeOfficeSettledStatusFrontendController.submitStatusCheckByNino()
        ))

    case StatusFound(_, query, result) =>
      Ok(statusFoundPage(StatusFoundPageContext(query, result, getCallFor(Start))))

    case StatusNotAvailable(_, query) =>
      Ok(statusNotAvailablePage(StatusNotAvailablePageContext(query, getCallFor(Start))))

    case StatusCheckFailure(_, query, error) =>
      if (error.errCode == "ERR_CONFLICT")
        Ok(multipleMatchesFoundPage(query, error, getCallFor(StatusCheckByNino(Some(query))), getCallFor(Start)))
      else Ok(statusCheckFailurePage(query, error, getCallFor(StatusCheckByNino(Some(query))), getCallFor(Start)))

  }

  override implicit def context(implicit rh: RequestHeader): HeaderCarrier =
    appendJourneyId(super.hc)

  override def amendContext(headerCarrier: HeaderCarrier)(key: String, value: String): HeaderCarrier =
    headerCarrier.withExtraHeaders(key -> value)
}

object HomeOfficeSettledStatusFrontendController {

  import FormFieldMappings._

  val StatusCheckByNinoRequestForm = Form[StatusCheckByNinoRequest](
    mapping(
      "nino" -> uppercaseNormalizedText
        .verifying(validNino())
        .transform(Nino.apply, (n: Nino) => n.toString),
      "givenName"   -> trimmedName.verifying(validName("givenName", 1)),
      "familyName"  -> trimmedName.verifying(validName("familyName", 2)),
      "dateOfBirth" -> dateOfBirthMapping,
      "range"       -> ignored[Option[StatusCheckRange]](None)
    )(StatusCheckByNinoRequest.apply)(StatusCheckByNinoRequest.unapply))
}
