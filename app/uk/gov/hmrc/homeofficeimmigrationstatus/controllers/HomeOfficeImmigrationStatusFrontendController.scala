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

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
<<<<<<< HEAD:app/uk/gov/hmrc/homeofficesettledstatus/controllers/HomeOfficeSettledStatusFrontendController.scala
import uk.gov.hmrc.homeofficesettledstatus.config.AppConfig
import uk.gov.hmrc.homeofficesettledstatus.connectors.HomeOfficeSettledStatusProxyConnector
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State.{StatusCheckFailure, _}
import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckRange}
import uk.gov.hmrc.homeofficesettledstatus.services.HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier
import uk.gov.hmrc.homeofficesettledstatus.views.html._
import uk.gov.hmrc.homeofficesettledstatus.views.{StatusFoundPageContext, StatusNotAvailablePageContext}
=======
import uk.gov.hmrc.homeofficeimmigrationstatus.config.AppConfig
import uk.gov.hmrc.homeofficeimmigrationstatus.connectors.{FrontendAuthConnector, HomeOfficeImmigrationStatusProxyConnector}
import uk.gov.hmrc.homeofficeimmigrationstatus.journeys.HomeOfficeImmigrationStatusFrontendJourneyModel.State.{StatusCheckFailure, _}
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckRange}
import uk.gov.hmrc.homeofficeimmigrationstatus.services.HomeOfficeImmigrationStatusFrontendJourneyServiceWithHeaderCarrier
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html._
import uk.gov.hmrc.homeofficeimmigrationstatus.views.{StatusFoundPageContext, StatusNotAvailablePageContext}
>>>>>>> HOSS2-149 - Update all references of settled status to immigration status:app/uk/gov/hmrc/homeofficeimmigrationstatus/controllers/HomeOfficeImmigrationStatusFrontendController.scala
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.fsm.{JourneyController, JourneyIdSupport}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class HomeOfficeImmigrationStatusFrontendController @Inject()(
  override val messagesApi: MessagesApi,
  override val config: Configuration,
  override val journeyService: HomeOfficeImmigrationStatusFrontendJourneyServiceWithHeaderCarrier,
  override val actionBuilder: DefaultActionBuilder,
  val authConnector: AuthConnector,
  val env: Environment,
  homeOfficeImmigrationStatusProxyConnector: HomeOfficeImmigrationStatusProxyConnector,
  controllerComponents: MessagesControllerComponents,
  statusCheckByNinoPage: StatusCheckByNinoPage,
  statusFoundPage: StatusFoundPage,
  statusNotAvailablePage: StatusNotAvailablePage,
  statusCheckFailurePage: StatusCheckFailurePage,
  multipleMatchesFoundPage: MultipleMatchesFoundPage
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with AuthActions
    with JourneyController[HeaderCarrier] with JourneyIdSupport[HeaderCarrier] {

  import HomeOfficeImmigrationStatusFrontendController._
  import uk.gov.hmrc.homeofficeimmigrationstatus.journeys.HomeOfficeImmigrationStatusFrontendJourneyModel._

  val AsStrideUser: WithAuthorised[String] = { implicit request =>
    authorisedWithStrideGroup(appConfig.authorisedStrideGroup)
  }

  // GET /
  val showStart: Action[AnyContent] =
    actions
      .whenAuthorisedWithRetrievals(AsStrideUser)
      .apply(Transitions.start)
      .display
      .andCleanBreadcrumbs() // reset navigation history

  // GET /check-with-nino
  val showStatusCheckByNino: Action[AnyContent] =
    actions
      .whenAuthorisedWithRetrievals(AsStrideUser)
      .apply(Transitions.showStatusCheckByNino)
      .display
      .andCleanBreadcrumbs() // reset navigation history

  // POST /check-with-nino
  val submitStatusCheckByNino: Action[AnyContent] =
    actions
      .whenAuthorisedWithRetrievals(AsStrideUser)
      .bindForm(StatusCheckByNinoRequestForm)
      .applyWithRequest(
        implicit request =>
          Transitions.submitStatusCheckByNino(
            homeOfficeImmigrationStatusProxyConnector.statusPublicFundsByNino(_),
            appConfig.defaultQueryTimeRangeInMonths
        ))

  // GET /status-found
  val showStatusFound: Action[AnyContent] =
    actions
      .whenAuthorised(AsStrideUser)
      .show[StatusFound]
      .orRollback

  // GET /status-not-available
  val showStatusNotAvailable: Action[AnyContent] =
    actions
      .whenAuthorised(AsStrideUser)
      .show[StatusNotAvailable]
      .orRollback

  // GET /status-check-failure
  val showStatusCheckFailure: Action[AnyContent] =
    actions
      .whenAuthorised(AsStrideUser)
      .show[StatusCheckFailure]
      .orRollback

  /** Function from the `State` to the `Call` (route),
    * used by play-fsm internally to create redirects.
    */
  override def getCallFor(state: State)(implicit request: Request[_]): Call = state match {
    case Start =>
      routes.HomeOfficeImmigrationStatusFrontendController.showStart
    case _: StatusCheckByNino =>
      routes.HomeOfficeImmigrationStatusFrontendController.showStatusCheckByNino
    case _: StatusNotAvailable =>
      routes.HomeOfficeImmigrationStatusFrontendController.showStatusNotAvailable
    case _: StatusFound =>
      routes.HomeOfficeImmigrationStatusFrontendController.showStatusFound
    case _: StatusCheckFailure =>
      routes.HomeOfficeImmigrationStatusFrontendController.showStatusCheckFailure
  }

  import uk.gov.hmrc.play.fsm.OptionalFormOps._

  /** Function from the `State` to the `Result`,
    * used by play-fsm internally to render the actual content.
    */
  override def renderState(state: State, breadcrumbs: List[State], formWithErrors: Option[Form[_]])(
    implicit
    request: Request[_]): Result = state match {

    case Start =>
      Redirect(getCallFor(StatusCheckByNino()))

    case StatusCheckByNino(maybeQuery) =>
      Ok(
        statusCheckByNinoPage(
          formWithErrors.or(
            maybeQuery
              .map(query => StatusCheckByNinoRequestForm.fill(query))
              .getOrElse(StatusCheckByNinoRequestForm)
          ),
          routes.HomeOfficeImmigrationStatusFrontendController.submitStatusCheckByNino
        )
      )

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

object HomeOfficeImmigrationStatusFrontendController {

  import FormFieldMappings._

  val StatusCheckByNinoRequestForm: Form[StatusCheckByNinoRequest] = Form[StatusCheckByNinoRequest](
    mapping(
      "nino" -> uppercaseNormalizedText
        .verifying(validNino())
        .transform(Nino.apply, (n: Nino) => n.toString),
      "givenName"   -> trimmedName.verifying(validName("givenName", 1)),
      "familyName"  -> trimmedName.verifying(validName("familyName", 2)),
      "dateOfBirth" -> dateOfBirthMapping,
      "range"       -> ignored[Option[StatusCheckRange]](None)
    )(StatusCheckByNinoRequest.apply)(StatusCheckByNinoRequest.unapply)
  )
}
