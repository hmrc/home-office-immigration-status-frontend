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

package forms

import play.api.data.Forms.{mapping, of, optional, text}
import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation._
import uk.gov.hmrc.domain.Nino
import forms.helpers.ValidateHelper
import forms.helpers.ValidateHelper.cond
import java.time.LocalDate
import scala.util.Try
import play.api.data.validation.Constraints.minLength

trait FormFieldMappings {

  def validNino: Constraint[String] =
    ValidateHelper.validateField("error.nino.required", "error.nino.invalid-format")(nino => Nino.isValid(nino))

  val maxNameLen = 64

  val normalizedText: Mapping[String] = of[String].transform(_.replaceAll("\\s", ""), identity)
  val uppercaseNormalizedText: Mapping[String] = normalizedText.transform(_.toUpperCase, identity)
  val trimmedName: Mapping[String] = of[String].transform[String](_.trim.take(maxNameLen), identity)

  val allowedNameCharacters: Set[Char] = Set('-', '\'', ' ')

  def hasAllowedCharacters(fieldName: String): Constraint[String] =
    cond(s"error.$fieldName.invalid-format")(_.forall(ch =>
      Character.isLetter(ch) || allowedNameCharacters.contains(ch)))

  def validName(fieldName: String, minLenInc: Int): Mapping[String] =
    nonEmptyText(fieldName)
      .verifying(minLength(minLenInc, s"error.$fieldName.length"))
      .verifying(hasAllowedCharacters(fieldName))

  def nonEmptyText(fieldName: String): Mapping[String] =
    optional(text)
      .verifying(
        s"error.$fieldName.required",
        _.exists(_.trim.nonEmpty)
      )
      .transform(_.get.trim, Some.apply)

  private val validateIsRealDate: Constraint[(String, String, String)] =
    cond("error.dateOfBirth.invalid-format") {
      case (year, month, day) =>
        Try(LocalDate.of(year.toInt, month.toInt, day.toInt)).isSuccess
    }

  private val validateNotToday: Constraint[LocalDate] =
    cond[LocalDate]("error.dateOfBirth.invalid-format")(_.isBefore(LocalDate.now()))

  private val formatDateFromFields: (String, String, String) => (String, String, String) = (y, month, day) => {
    val year = if (y.length == 2) "19" + y else y
    (year, month, day)
  }

  private val asDate: (String, String, String) => LocalDate = (y, m, d) => LocalDate.of(y.toInt, m.toInt, d.toInt)
  private val asTuple: LocalDate => (String, String, String) = d =>
    (d.getYear.toString, d.getMonthValue.toString, d.getDayOfMonth.toString)

  def dobFieldsMapping: Mapping[LocalDate] =
    mapping(
      "year"  -> of[String].transform[String](_.trim, identity),
      "month" -> of[String].transform[String](_.trim, identity),
      "day"   -> of[String].transform[String](_.trim, identity)
    )(formatDateFromFields)(Some.apply)
      .verifying(validateIsRealDate)
      .transform(asDate.tupled, asTuple)
      .verifying(validateNotToday)
}
