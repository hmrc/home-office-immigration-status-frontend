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

package models

import play.api.libs.json.{Format, JsObject, Json, Reads, Writes}
import uk.gov.hmrc.domain.Nino
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.i18n.Messages

sealed trait Search

object Search {
  implicit val reads: Reads[Search] = Json.reads[Search]
  implicit val writes: Writes[Search] = (o: Search) => Json.toJson(o)(Json.writes[Search]).as[JsObject] - "_type"
}

final case class NinoSearch(
  nino: Nino,
  givenName: String,
  familyName: String,
  dateOfBirth: String,
  statusCheckRange: StatusCheckRange
) extends Search

object NinoSearch {
  private val ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def apply(
    nino: Nino,
    givenName: String,
    familyName: String,
    dateOfBirth: LocalDate,
    statusCheckRange: StatusCheckRange) =
    new NinoSearch(nino, givenName, familyName, dateOfBirth.format(ISO8601), statusCheckRange)

  implicit val formats: Format[NinoSearch] = Json.format[NinoSearch]
}

final case class MrzSearch(
  documentType: String,
  documentNumber: String,
  dateOfBirth: LocalDate,
  nationality: String,
  statusCheckRange: StatusCheckRange
) extends Search

object MrzSearch {
  implicit val formats: Format[MrzSearch] = Json.format[MrzSearch]

  val Passport = "PASSPORT"
  val EuropeanNationalIdentityCard = "NAT"
  val BiometricResidencyCard = "BRC"
  val BiometricResidencyPermit = "BRP"

  def documentTypeToMessageKey(documentType: String)(implicit messages: Messages): String = documentType match {
    case Passport                     => messages("lookup.passport")
    case EuropeanNationalIdentityCard => messages("lookup.euni")
    case BiometricResidencyCard       => messages("lookup.res.card")
    case BiometricResidencyPermit     => messages("lookup.res.permit")
    case docType                      => docType
  }
}
