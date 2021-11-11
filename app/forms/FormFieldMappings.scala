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

import play.api.data.Forms.{mapping, of}
import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation._
import uk.gov.hmrc.domain.Nino
import forms.helpers.ValidateHelper
import forms.helpers.ValidateHelper.cond

import java.time.LocalDate
import scala.util.Try

trait FormFieldMappings {

  def validNino: Constraint[String] =
    ValidateHelper.validateField("error.nino.required", "error.nino.invalid-format")(nino => Nino.isValid(nino))

  val maxNameLen = 64

  val normalizedText: Mapping[String] = of[String].transform(_.replaceAll("\\s", ""), identity)
  val uppercaseNormalizedText: Mapping[String] = normalizedText.transform(_.toUpperCase, identity)
  val trimmedName: Mapping[String] = of[String].transform[String](_.trim.take(maxNameLen), identity)

  val allowedNameCharacters: Set[Char] = Set('-', '\'', ' ')

  def validName(fieldName: String, minLenInc: Int): Constraint[String] =
    Constraint[String] { fieldValue: String =>
      nonEmpty(fieldName)(fieldValue) match {
        case i @ Invalid(_) => i
        case Valid =>
          if (fieldValue.length >= minLenInc && fieldValue.forall(
                ch => Character.isLetter(ch) || allowedNameCharacters.contains(ch)))
            Valid
          else
            Invalid(ValidationError(s"error.$fieldName.invalid-format"))
      }
    }

  def nonEmpty(fieldName: String): Constraint[String] =
    Constraint[String]("constraint.required") { s =>
      Option(s)
        .filter(_.trim.nonEmpty)
        .fold[ValidationResult](Invalid(ValidationError(s"error.$fieldName.required")))(_ => Valid)
    }

  private def parseDateIntoFields(date: String): Option[(String, String, String)] = {
    val ydm: Array[String] = date.split('-') ++ Array("", "")
    Some((ydm(0), ydm(1), ydm(2)))
  }

  private val validateIsRealDate: Constraint[String] =
    cond("error.dateOfBirth.invalid-format")(data => {
      Try(LocalDate.parse(data)).isSuccess
    })

  private val validateNotToday: Constraint[LocalDate] =
    cond[LocalDate]("error.dateOfBirth.invalid-format")(_.isBefore(LocalDate.now()))

  private val formatDateFromFields: (String, String, String) => String = (y, m, d) => {
    val year = if (y.length == 2) "19" + y else y
    val month = if (m.length == 1) "0" + m else m
    val day = if (d.length == 1) "0" + d else d
    s"$year-$month-$day"
  }

  def dobFieldsMapping: Mapping[LocalDate] =
    mapping(
      "year"  -> of[String].transform[String](_.trim, identity),
      "month" -> of[String].transform[String](_.trim, identity),
      "day"   -> of[String].transform[String](_.trim, identity)
    )(formatDateFromFields)(parseDateIntoFields)
      .verifying(nonEmpty("error.dateOfBirth.required"))
      .verifying(validateIsRealDate)
      .transform(LocalDate.parse, (d: LocalDate) => d.toString)
      .verifying(validateNotToday)
}
