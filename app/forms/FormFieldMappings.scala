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

  private val validateIsRealDate: Constraint[(String, String, String)] =
    cond("error.dateOfBirth.invalid-format") {
      case (year, month, day) =>
        Try(LocalDate.of(year.toInt, month.toInt, day.toInt)).isSuccess
    }

  private val validateInThePast: Constraint[LocalDate] =
    cond[LocalDate]("error.dateOfBirth.past")(_.isBefore(LocalDate.now()))

  private val asDate: (String, String, String) => LocalDate = (y, m, d) => LocalDate.of(y.toInt, m.toInt, d.toInt)
  private val asTuple: LocalDate => (String, String, String) = d =>
    (d.getYear.toString, d.getMonthValue.toString, d.getDayOfMonth.toString)

  def isInt(str: String): Boolean = Try(str.toInt).isSuccess

  def isNotZero(str: String): Boolean = Try(str.toInt).map(_ != 0).getOrElse(true)

  def validateNonEmptyInt(field: String, minLengthVal: Option[Int] = None): Constraint[String] = Constraint[String] {
    fieldValue: String =>
      val baseValidations: Seq[Constraint[String]] = Seq(
        nonEmpty(s"error.dateOfBirth.$field.required"),
        cond[String](s"error.dateOfBirth.$field.invalid")(isInt),
        cond[String](s"error.dateOfBirth.$field.required")(isNotZero)
      )

      val validations = minLengthVal match {
        case None      => baseValidations
        case Some(len) => baseValidations :+ minLength(len, s"error.dateOfBirth.$field.length")
      }

      validations.foldLeft[ValidationResult](Valid) { (result, condition) =>
        result match {
          case Valid => condition(fieldValue)
          case _     => result
        }
      }
  }

  def dateComponent(field: String, minLengthVal: Option[Int] = None): Mapping[String] =
    of[String].transform[String](_.trim, identity).verifying(validateNonEmptyInt(field, minLengthVal))

  def dobFieldsMapping: Mapping[LocalDate] =
    tuple(
      "year"  -> dateComponent("year", Some(4)),
      "month" -> dateComponent("month"),
      "day"   -> dateComponent("day")
    ).verifying(validateIsRealDate)
      .transform(asDate.tupled, asTuple)
      .verifying(validateInThePast)
}
