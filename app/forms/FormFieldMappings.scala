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

import play.api.data.Forms.{of, optional, text, tuple}
import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation._
import uk.gov.hmrc.domain.Nino
import forms.helpers.ValidateHelper
import forms.helpers.ValidateHelper.cond
import java.time.LocalDate
import scala.util.Try

trait FormFieldMappings extends Constraints {

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

  private val validateIsRealDate: Constraint[(Int, Int, Int)] =
    cond("error.dateOfBirth.invalid-format") {
      case (day, month, year) =>
        Try(LocalDate.of(year, month, day)).isSuccess
    }

  private val validateInThePast: Constraint[LocalDate] =
    cond[LocalDate]("error.dateOfBirth.past")(_.isBefore(LocalDate.now()))

  private val asDate: (Int, Int, Int) => LocalDate = (d, m, y) => LocalDate.of(y, m, d)
  private val asTuple: LocalDate => (Int, Int, Int) = d => (d.getDayOfMonth, d.getMonthValue, d.getYear)

  def isInt(str: String): Boolean = Try(str.trim.toInt).isSuccess

  def isNotZero(int: Int): Boolean = int != 0

  def dateComponent(field: String, maxValue: Int, minValue: Int = 0): Mapping[Int] =
    nonEmptyText(s"dateOfBirth.$field")
      .verifying(cond[String](s"error.dateOfBirth.$field.invalid")(isInt))
      .transform[Int](_.toInt, _.toString)
      .verifying(cond[Int](s"error.dateOfBirth.$field.zero")(isNotZero))
      .transform[Int](identity, identity)
      .verifying(min(minValue = minValue, errorMessage = s"error.dateOfBirth.$field.min"))
      .verifying(max(maxValue = maxValue, errorMessage = s"error.dateOfBirth.$field.max"))

  def dobFieldsMapping: Mapping[LocalDate] =
    tuple(
      "day"   -> dateComponent("day", 31),
      "month" -> dateComponent("month", 12),
      "year"  -> dateComponent("year", 3000, 1000)
    ).verifying(validateIsRealDate)
      .transform(asDate.tupled, asTuple)
      .verifying(validateInThePast)
}
