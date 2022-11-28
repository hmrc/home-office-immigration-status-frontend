/*
 * Copyright 2022 HM Revenue & Customs
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

import config.Countries
import models._
import play.api.Logging
import play.api.i18n.Messages
import viewmodels.{RowViewModel => Row}
import views.StatusFoundPageContext.RichMessages

final case class StatusFoundPageContext(query: SearchFormModel, result: StatusCheckResult) {

  val mostRecentStatus: Option[ImmigrationStatus] = result.mostRecentStatus
  val previousStatuses: Seq[ImmigrationStatus]    = result.previousStatuses

  def immigrationStatusRows(implicit messages: Messages): Seq[Row] =
    Seq(
      immigrationRoute.map(route => Row("immigrationRoute", "status-found.route", route)),
      mostRecentStatus.map(s =>
        Row("startDate", "status-found.startDate", DateFormat.format(messages.lang.locale)(s.statusStartDate))
      ),
      mostRecentStatus.flatMap(s =>
        s.statusEndDate.map(date =>
          Row("expiryDate", "status-found.endDate", DateFormat.format(messages.lang.locale)(date))
        )
      ),
      if (!hasRecourseToPublicFunds) {
        Some(Row("recourse-text", "status-found.norecourse", messages("status-found.no")))
      } else {
        None
      }
    ).flatten

  def detailRows(countries: Countries)(implicit messages: Messages): Seq[Row] = query match {
    case q: NinoSearchFormModel =>
      Seq(
        Row("nino", "generic.nino", q.nino.nino),
        Row("nationality", "generic.nationality", countries.getCountryNameFor(result.nationality)),
        Row("dob", "generic.dob", result.dobFormatted(messages.lang.locale))
      )
    case q: MrzSearchFormModel =>
      val documentTypeText = MrzSearch.documentTypeToMessageKey(q.documentType)
      Seq(
        Row("documentType", "lookup.identity.label", documentTypeText),
        Row("documentNumber", "lookup.mrz.label", q.documentNumber),
        Row("nationality", "generic.nationality", countries.getCountryNameFor(result.nationality)),
        Row("dob", "generic.dob", result.dobFormatted(messages.lang.locale))
      )
  }

  def hasRecourseToPublicFunds: Boolean = !mostRecentStatus.exists(_.noRecourseToPublicFunds)

  val prefix                                              = "status-found.current."
  def key(key: String, status: ImmigrationStatus): String = prefix + key + status.expiredMsg

  def currentStatusLabel(implicit messages: Messages): String =
    mostRecentStatus match {
      case Some(status) =>
        val defaultKey = prefix + "hasFBIS"
        val default    = messages(defaultKey, status.productType, status.immigrationStatus)
        val eusPrefix  = if (status.isEUS) "EUS." else "nonEUS."
        messages.getOrElse(key(eusPrefix + status.immigrationStatus, status), defaultKey, default)
      case None =>
        messages(prefix + "noStatus")
    }

  def immigrationRoute(implicit messages: Messages): Option[String] =
    mostRecentStatus.map(status => StatusFoundPageContext.getImmigrationRouteLabel(status.productType))

  val isZambrano: Boolean = mostRecentStatus.exists(_.isEUS) && !EEACountries.countries.contains(result.nationality)
}

object StatusFoundPageContext extends Logging {

  implicit class RichMessages(val messages: Messages) extends AnyVal {
    def getOrElse(key: String, defaultKey: String, defaultValue: String): String =
      if (messages.isDefinedAt(key)) {
        messages(key)
      } else {
        logger.warn(
          s"$key was not defined. Consider adding this to the messages file. Using default key '$defaultKey' placeholder text."
        )
        defaultValue
      }
  }

  def getImmigrationRouteLabel(productType: String)(implicit messages: Messages): String =
    messages.getOrElse(s"immigration.${productType.toLowerCase}", "NO_KEY", productType)

  def getStatusLabel(status: ImmigrationStatus)(implicit messages: Messages): String = {
    val prefix = if (status.isEUS) "immigration.EUS." else "immigration.nonEUS."
    messages.getOrElse(s"$prefix${status.immigrationStatus.toLowerCase}", "NO_KEY", status.immigrationStatus)
  }

  def immigrationStatusLabel(status: ImmigrationStatus)(implicit messages: Messages): String = {
    val productString = getImmigrationRouteLabel(status.productType)
    val statusString  = getStatusLabel(status)
    s"$productString - $statusString"
  }

}
