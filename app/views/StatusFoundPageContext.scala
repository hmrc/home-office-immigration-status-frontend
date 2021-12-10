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

package views

import play.api.i18n.Messages
import viewmodels.{RowViewModel => Row}
import views.StatusFoundPageContext.RichMessages
import models.{ImmigrationStatus, MrzSearchFormModel, NinoSearchFormModel, SearchFormModel, StatusCheckResult}

final case class StatusFoundPageContext(query: SearchFormModel, result: StatusCheckResult) {

  val mostRecentStatus: Option[ImmigrationStatus] = result.mostRecentStatus
  val previousStatuses: Seq[ImmigrationStatus] = result.previousStatuses

  def immigrationStatusRows(implicit messages: Messages): Seq[Row] =
    Seq(
      immigrationRoute.map(route => Row("immigrationRoute", "status-found.route", route)),
      mostRecentStatus.map(s =>
        Row("startDate", "status-found.startDate", DateFormat.format(messages.lang.locale)(s.statusStartDate))),
      mostRecentStatus.flatMap(s =>
        s.statusEndDate.map(date =>
          Row("expiryDate", "status-found.endDate", DateFormat.format(messages.lang.locale)(date)))),
      if (!hasRecourseToPublicFunds)
        Some(Row("recourse-text", "status-found.norecourse", messages("status-found.no")))
      else None
    ).flatten

  def detailRows(implicit messages: Messages): Seq[Row] = query match {
    case q: NinoSearchFormModel =>
      Seq(
        Row("nino", "generic.nino", q.nino.nino),
        Row("dob", "generic.dob", result.dobFormatted(messages.lang.locale)),
        Row("nationality", "generic.nationality", result.countryName)
      )
    case q: MrzSearchFormModel => Nil
  }

  def hasRecourseToPublicFunds: Boolean = !mostRecentStatus.exists(_.noRecourseToPublicFunds)

  val prefix = "status-found.current."
  def key(key: String, status: ImmigrationStatus): String = prefix + key + status.expiredMsg

  def currentStatusLabel(implicit messages: Messages): String =
    mostRecentStatus match {
      case Some(status) =>
        val default = messages(prefix + "hasFBIS", status.productType, status.immigrationStatus)
        val eusPrefix = if (status.isEUS) "EUS." else "nonEUS."
        messages.getOrElse(key(eusPrefix + status.immigrationStatus, status), default)
      case None =>
        messages(prefix + "noStatus")
    }

  def getImmigrationRoute(productType: String)(implicit messages: Messages) =
    messages.getOrElse(s"immigration.${productType.toLowerCase}", productType)

  def immigrationRoute(implicit messages: Messages) =
    mostRecentStatus.map(status => getImmigrationRoute(status.productType))
}

object StatusFoundPageContext {

  implicit class RichMessages(val messages: Messages) extends AnyVal {
    def getOrElse(key: String, default: String): String = {
      val newKey = replacePipesInKey(key)
      if (messages.isDefinedAt(newKey)) messages(newKey) else default
    }
  }

  def replacePipesInKey(key: String): String = key.replace('|', '.')

  def immigrationStatusLabel(productType: String, status: String)(implicit messages: Messages): String =
    messages.getOrElse(
      s"immigration.${productType.toLowerCase}.${status.toLowerCase}",
      s"$productType - $status"
    )
}
