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

import play.api.data.Forms.{mapping, of, optional, text, tuple}
import play.api.data.{FieldMapping, Mapping}
import play.api.data.format.Formats._
import play.api.data.validation._
import uk.gov.hmrc.domain.Nino
import forms.helpers.ValidateHelper
import forms.helpers.ValidateHelper.{cond, nonEmpty}
import java.time.LocalDate

import scala.util.{Success, Try}

trait FormFieldMappings extends Constraints {

  def validNino: Constraint[String] =
    ValidateHelper.validateField("error.nino.required", "error.nino.invalid-format")(nino => Nino.isValid(nino))

  val maxNameLen = 64

  val normalizedText: Mapping[String] = of[String].transform(_.replaceAll("\\s", ""), identity)
  val uppercaseNormalizedText: Mapping[String] = normalizedText.transform(_.toUpperCase, identity)
  val trimmedName: Mapping[String] = of[String].transform[String](_.trim.take(maxNameLen), identity)

  val allowedNameCharacters: Set[Char] = Set('-', '\'', ' ')

  def validName(fieldName: String, minLenInc: Int): Constraint[String] =
    ValidateHelper.validateField(s"error.$fieldName.required", s"error.$fieldName.invalid-format")(fieldVal =>
      fieldVal.length >= minLenInc && fieldVal.forall(ch =>
        Character.isLetter(ch) || allowedNameCharacters.contains(ch)))

  def nonEmptyText(fieldName: String): Mapping[String] =
    optional(text)
      .verifying(
        s"error.$fieldName.required",
        _.exists(_.trim.nonEmpty)
      )
      .transform(_.get, Some.apply)

  private val validateIsRealDate: Constraint[(Int, Int, Int)] =
    cond("error.dateOfBirth.invalid-format") {
      case (year, month, day) =>
        Try(LocalDate.of(year, month, day)).isSuccess
    }

  private val validateInThePast: Constraint[LocalDate] =
    cond[LocalDate]("error.dateOfBirth.past")(_.isBefore(LocalDate.now()))

  private val asDate: (Int, Int, Int) => LocalDate = (y, m, d) => LocalDate.of(y, m, d)
  private val asTuple: LocalDate => (Int, Int, Int) = d => (d.getYear, d.getMonthValue, d.getDayOfMonth)

  def isInt(str: String): Boolean = Try(str.trim.toInt).isSuccess

  def isNotZero(int: Int): Boolean = int != 0

  def nonEmptyConstraint(field: String): Constraint[String] = nonEmpty(s"error.dateOfBirth.$field.required")

  def dateComponent(field: String, minLengthVal: Int = 0): Mapping[Int] =
    nonEmptyText(s"dateOfBirth.$field")
      .verifying(cond[String](s"error.dateOfBirth.$field.invalid")(isInt))
      .transform[Int](_.toInt, _.toString)
      .verifying(cond[Int](s"error.dateOfBirth.$field.required")(isNotZero))
      .verifying(min(minValue = minLengthVal, errorMessage = s"error.dateOfBirth.$field.min"))

  def dobFieldsMapping: Mapping[LocalDate] =
    tuple(
      "year"  -> dateComponent("year", 1000),
      "month" -> dateComponent("month"),
      "day"   -> dateComponent("day")
    ).verifying(validateIsRealDate)
      .transform(asDate.tupled, asTuple)
      .verifying(validateInThePast)
}
