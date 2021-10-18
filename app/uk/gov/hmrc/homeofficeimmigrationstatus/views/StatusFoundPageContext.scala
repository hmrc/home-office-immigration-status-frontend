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

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.homeofficeimmigrationstatus.models.ImmigrationStatus._
import uk.gov.hmrc.homeofficeimmigrationstatus.viewmodels.{RowViewModel => Row}
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}

final case class StatusFoundPageContext(
  query: StatusCheckByNinoRequest,
  result: StatusCheckResult,
  searchAgainCall: Call) {

  val mostRecentStatus: Option[ImmigrationStatus] = result.mostRecentStatus
  val previousStatuses: Seq[ImmigrationStatus] = result.previousStatuses

  def immigrationStatusRows(implicit messages: Messages): Seq[Row] =
    Seq(
      immigrationRoute.map(route => Row("immigrationRoute", "status-found.route", route)),
      mostRecentStatus.map(s =>
        Row("startDate", "status-found.startDate", DateFormat.format(messages.lang.locale)(s.statusStartDate))),
      mostRecentStatus.flatMap(s =>
        s.statusEndDate.map(date =>
          Row("expiryDate", "status-found.expiryDate", DateFormat.format(messages.lang.locale)(date)))),
      if (displayRecourseToPublicFunds)
        Some(Row("recourse-text", "status-found.norecourse", messages("status-found.no")))
      else None
    ).flatten

  def detailRows(implicit messages: Messages): Seq[Row] =
    Seq(
      Row("nino", "generic.nino", query.nino.formatted),
      Row("dob", "generic.dob", result.dobFormatted(messages.lang.locale)),
      Row("nationality", "generic.nationality", result.countryName)
    )

  def displayRecourseToPublicFunds: Boolean = mostRecentStatus.exists(_.noRecourseToPublicFunds)

  def currentStatusLabel(implicit messages: Messages): String = {
    val prefix = "status-found.current."
    mostRecentStatus match {
      case Some(status) =>
        def key(key: String): String = prefix + key + status.expiredMsg
        (status.productType, status.immigrationStatus) match {
          case (EUS, LTR)                       => messages(key("EUS.LTR"))
          case (EUS, ILR)                       => messages(key("EUS.ILR"))
          case (_, LTR)                         => messages(key("nonEUS.LTR"))
          case (_, ILR)                         => messages(key("nonEUS.ILR"))
          case (_, LTE)                         => messages(key("nonEUS.LTE"))
          case (EUS, COA_IN_TIME_GRANT)         => messages(prefix + "EUS.COA")
          case (EUS, POST_GRACE_PERIOD_COA)     => messages(prefix + "EUS.COA")
          case (FRONTIER_WORKER, PERMIT)        => messages(key("FRONTIER_WORKER.PERMIT"))
          case (productType, immigrationStatus) => messages(prefix + "hasFBIS", productType, immigrationStatus)
        }
      case None => messages(prefix + "noStatus")
    }
  }

  def getImmigrationRoute(productType: String)(implicit messages: Messages) =
    productType match {
      case "EUS"             => messages("immigration.eus")
      case "STUDY"           => messages("immigration.study")
      case "DEPENDANT"       => messages("immigration.dependant")
      case "WORK"            => messages("immigration.work")
      case "FRONTIER_WORKER" => messages("immigration.frontier")
      case "BNO"             => messages("immigration.bno")
      case "BNO_LOTR"        => messages("immigration.bno_lotr")
      case "GRADUATE"        => messages("immigration.graduate")
      case "SPORTSPERSON"    => messages("immigration.sportsperson")
      case "SETTLEMENT"      => messages("immigration.settlement")
      case "TEMP_WORKER"     => messages("immigration.temp_worker")
      case _                 => productType
    }

  def immigrationRoute(implicit messages: Messages) =
    mostRecentStatus.map(status => getImmigrationRoute(status.productType))
}

object StatusFoundPageContext {
  def immigrationStatusLabel(productType: String, status: String)(implicit messages: Messages): String =
    (productType, status) match {
      case (EUS, ILR)                   => messages("immigration.eu.ilr")
      case (EUS, LTR)                   => messages("immigration.eu.ltr")
      case (STUDY, LTE)                 => messages("immigration.study.lte")
      case (STUDY, LTR)                 => messages("immigration.study.ltr")
      case (DEPENDANT, LTE)             => messages("immigration.dependant.lte")
      case (DEPENDANT, LTR)             => messages("immigration.dependant.ltr")
      case (WORK, LTE)                  => messages("immigration.worker.lte")
      case (WORK, LTR)                  => messages("immigration.worker.ltr")
      case (FRONTIER_WORKER, PERMIT)    => messages("immigration.frontier-worker.permit")
      case (BNO, LTE)                   => messages("immigration.bno.lte")
      case (BNO, LTR)                   => messages("immigration.bno.ltr")
      case (BNO_LOTR, LTE)              => messages("immigration.bno.lotr.lte")
      case (BNO_LOTR, LTR)              => messages("immigration.bno.lotr.ltr")
      case (GRADUATE, LTR)              => messages("immigration.graduate.ltr")
      case (EUS, COA_IN_TIME_GRANT)     => messages("immigration.eus.coa")
      case (EUS, POST_GRACE_PERIOD_COA) => messages("immigration.eus.post")
      case (SPORTSPERSON, LTR)          => messages("immigration.sportsperson.ltr")
      case (SPORTSPERSON, LTE)          => messages("immigration.sportsperson.lte")
      case (SETTLEMENT, ILR)            => messages("immigration.bno.settlement")
      case (TEMP_WORKER, LTR)           => messages("immigration.temp-worker.ltr")
      case (TEMP_WORKER, LTE)           => messages("immigration.temp-worker.lte")
      case _                            => s"$productType - $status"
    }
}
