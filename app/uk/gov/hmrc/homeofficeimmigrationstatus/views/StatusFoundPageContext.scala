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

  //todo change name of this when grouped properly
  def stuffRows(implicit messages: Messages) =
    Seq(
      immigrationRoute.map(route => Row("immigrationRoute", "status-found.route", route)),
      if (displayRecourseToPublicFunds)
        Some(Row("recourse-text", "status-found.norecourse", messages("status-found.no")))
      else None
    ).flatten

  def detailRows(implicit messages: Messages) =
    Seq(
      Some(Row("nino", "generic.nino", query.nino.formatted)),
      Some(Row("dob", "generic.dob", result.dobFormatted(messages.lang.locale))),
      Some(Row("nationality", "generic.nationality", result.countryName)),
      mostRecentStatus.map(s =>
        Row("startDate", "status-found.startDate", DateFormat.format(messages.lang.locale)(s.statusStartDate))),
      mostRecentStatus.flatMap(s =>
        s.statusEndDate.map(date =>
          Row("expiryDate", "status-found.expiryDate", DateFormat.format(messages.lang.locale)(date))))
    ).flatten

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

  def getImmigrationStatus(productType: String, immigrationStatus: String)(implicit messages: Messages) =
    productType match {
      case "EUS" => immigrationStatus match {
        case "ILR" => "%a - %b".format(messages("immigration.eus"),messages("immigration.ilr"))
        case "LTR" => messages("immigration.eus") + messages("immigration.ltr")
        case _ => messages("immigration.eus")
      }
      case "STUDY" => immigrationStatus match {
        case "LTE" => messages("immigration.study") + messages("immigration.ltefbis")
        case "LTR" => messages("immigration.study") + messages("immigration.ltrfbis")
        case _ => messages("immigration.study")
      }
      case "DEPENDANT" => immigrationStatus match {
        case "LTE" => messages("immigration.dependant") + messages("immigration.ltefbis")
        case "LTR" => messages("immigration.dependant") + messages("immigration.ltrfbis")
        case _ => messages("immigration.dependant")
      }
      case "WORK" => immigrationStatus match {
        case "LTE" => messages("immigration.work") + messages("immigration.ltefbis")
        case "LTR" => messages("immigration.work") + messages("immigration.ltrfbis")
        case _ => messages("immigration.work")
      }
      case "FRONTIER_WORKER" => immigrationStatus match {
        case "PERMIT" => messages("immigration.frontier") + messages("immigration.permit")
        case _ => messages("immigration.frontier")
      }
      case "BNO" => immigrationStatus match {
        case "LTE" => messages("immigration.bno") + messages("immigration.ltefbis")
        case "LTR" => messages("immigration.bno") + messages("immigration.ltrfbis")
        case _ => messages("immigration.bno")
      }
      case "BNO_LOTR" => immigrationStatus match {
        case "LTE" => messages("immigration.bno_lotr") + messages("immigration.bnolotr")
        case "LTR" => messages("immigration.bno_lotr") + messages("immigration.bnolotr")
        case _ => messages("immigration.bno_lotr")
      }
      case "GRADUATE" => immigrationStatus match {
        case "LTR" => messages("immigration.graduate") + messages("immigration.ltrfbis")
        case _ => messages("immigration.graduate")
      }
      case "EU" => immigrationStatus match {
        case "COA_IN_TIME_GRANT" => messages("immigration.eu") + messages("immigration.coa")
        case "POST_GRACE_PERIOD_COA_GRANT" => messages("immigration.eu") + messages("immigration.post")
        case _ => messages("immigration.eu")
      }
      case "SPORTSPERSON" => immigrationStatus match {
        case "LTE" => messages("immigration.sportsperson") + messages("immigration.ltefbis")
        case "LTR" => messages("immigration.sportsperson") + messages("immigration.ltrfbis")
        case _ => messages("immigration.sportsperson")
      }
      case "SETTLEMENT" => immigrationStatus match {
        case "ILR" => messages("immigration.settlement") + messages("immigration.settle")
        case _ => messages("immigration.settlement")
      }
      case "TEMP_WORKER" => immigrationStatus match {
        case "LTE" => messages("immigration.temp_worker") + messages("immigration.ltefbis")
        case "LTR" => messages("immigration.temp_worker") + messages("immigration.ltrfbis")
        case _ => messages("immigration.temp_worker")
      }
      case _                 => productType
    }

  def buildStatusMessage(productType: String, status: String)(implicit messages: Messages) = {

    productType match {
      case "EUS" => status match {
        case "ILR" => messages("immigration.ilr")
        case "LTR" => messages("immigration.ltr")
      }
      case "BNO_LOTR" => messages("immigration.bnolotr")
      case "SETTLEMENT" => messages("immigration.settle")
      case _ => status match {
        case "LTR" => messages("immigration.ltrfbis")
        case "LTE" => messages("immigration.ltefbis")
        case "PERMIT" => messages("immigration.permit")
        case "COA_IN_TIME_GRANT" => messages("immigration.coa")
        case "POST_GRACE_PERIOD_COA_GRANT" => messages("immigration.post")
        case _ => status
      }
    }
  }

  def buildStatusMessageSecondAttempt(productType: String, status: String)(implicit messages: Messages) =
    (productType, status) match {
      case ("EUS", "ILR") => messages("immigration.ilr")
      case ("EUS", "ILR") => messages("immigration.ilr")
      case ("BNO_LOTR", _) => messages("immigration.bnolotr")
      case ("SETTLEMENT", _) => messages("immigration.settle")
      case (_,"LTR") => messages("immigration.ltrfbis")
      case (_,"LTE") => messages("immigration.ltefbis")
      case (_,"PERMIT") => messages("immigration.permit")
      case (_,"COA_IN_TIME_GRANT") => messages("immigration.coa")
      case (_,"POST_GRACE_PERIOD_COA_GRANT") => messages("immigration.post")
    }

  def immigrationStatusSecondAttempt(implicit messages: Messages) = {
    mostRecentStatus.map(status => getImmigrationRoute(status.productType) + " - " +
      buildStatusMessage(status.productType,status.immigrationStatus))
  }

  def immigrationStatusThirdAttempt(implicit messages: Messages) = {
    mostRecentStatus.map(status => getImmigrationRoute(status.productType) + " - " +
      buildStatusMessage(status.productType,status.immigrationStatus))
  }

  def immigrationRoute(implicit messages: Messages) =
    mostRecentStatus.map(status => getImmigrationRoute(status.productType))

  def immigrationStatus(implicit messages: Messages) =
    mostRecentStatus.map(status => getImmigrationStatus(status.productType, status.immigrationStatus))
}

object StatusFoundPageContext {
  def immigrationStatusLabel(productType: String, status: String)(implicit messages: Messages): String =
    (productType, status) match {
      case (EUS, LTR) => messages("app.status.EUS_LTR")
      case (EUS, ILR) => messages("app.status.EUS_ILR")
      case _          => s"$productType + $status"
    }
}
