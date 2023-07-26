/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.helpers.ValidateHelper.cond
import play.api.data.Forms.{optional, text, tuple}
import play.api.data.validation._
import play.api.data.{Form, FormError, Mapping}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import scala.util.Try

trait FormFieldMappings extends Constraints {

  protected def validNino: Mapping[Nino] =
    nonEmptyText("nino")
      .transform[String](normaliseText, identity)
      .verifying(containsOnlyNumbersAndLetters)
      .transform[String](_.toUpperCase, identity)
      .verifying(isValidNino)
      .transform(Nino.apply, (n: Nino) => n.nino)

  private def isValidNino: Constraint[String] =
    cond("error.nino.invalid-format")(nino => Nino.isValid(nino))

  private def containsOnlyNumbersAndLetters: Constraint[String] =
    cond("error.nino.invalid-characters")(_.forall(Character.isLetterOrDigit))

  private val normaliseText: String => String = str => str.replaceAll("\\s", "")

  val allowedNameCharacters: Set[Char] = Set('-', '\'', ' ')

  private def hasAllowedCharacters(fieldName: String): Constraint[String] =
    cond(s"error.$fieldName.invalid-format")(
      _.forall(ch => Character.isLetter(ch) || allowedNameCharacters.contains(ch))
    )

  protected def validName(fieldName: String, minLenInc: Int): Mapping[String] =
    nonEmptyText(fieldName)
      .verifying(minLength(minLenInc, s"error.$fieldName.length"))
      .verifying(hasAllowedCharacters(fieldName))

  protected def nonEmptyText(fieldName: String): Mapping[String] =
    optional(text)
      .verifying(
        s"error.$fieldName.required",
        _.exists(_.trim.nonEmpty)
      )
      .transform(_.get.trim, Some.apply)

  private val validateIsRealDate: Constraint[(Int, Int, Int)] =
    cond("error.dateOfBirth.invalid-format") { case (day, month, year) =>
      Try(LocalDate.of(year, month, day)).isSuccess
    }

  private val validateInThePast: Constraint[LocalDate] =
    cond[LocalDate]("error.dateOfBirth.past")(_.isBefore(LocalDate.now()))

  private val asDate: (Int, Int, Int) => LocalDate  = (d, m, y) => LocalDate.of(y, m, d)
  private val asTuple: LocalDate => (Int, Int, Int) = d => (d.getDayOfMonth, d.getMonthValue, d.getYear)

  def isInt(str: String): Boolean = Try(str.trim.toInt).isSuccess

  def isNotZero(int: Int): Boolean = int != 0

  protected def dateComponent(field: String, maxValue: Int, minValue: Int = 0): Mapping[Int] =
    nonEmptyText(s"dateOfBirth.$field")
      .verifying(cond[String](s"error.dateOfBirth.$field.invalid")(isInt))
      .transform[Int](_.toInt, _.toString)
      .verifying(cond[Int](s"error.dateOfBirth.$field.zero")(isNotZero))
      .transform[Int](identity, identity)
      .verifying(min(minValue = minValue, errorMessage = s"error.dateOfBirth.$field.min"))
      .verifying(max(maxValue = maxValue, errorMessage = s"error.dateOfBirth.$field.max"))

  protected def dobFieldsMapping: Mapping[LocalDate] =
    tuple( //scalastyle:off magic.number
      "day"   -> dateComponent("day", 31),
      "month" -> dateComponent("month", 12),
      "year"  -> dateComponent("year", 3000, 1000)
    ).verifying(validateIsRealDate)
      .transform(asDate.tupled, asTuple)
      .verifying(validateInThePast)

  def collateDOBErrors[A](form: Form[A]): Form[A] =
    if (form.errors.count(_.key.contains("dateOfBirth")) > 1) {
      val required = form.errors.count(_.message.matches(""".*dateOfBirth.*\.required""")) == 3
      (form.errors.filterNot(_.key.contains("dateOfBirth")) :+ FormError(
        "dateOfBirth",
        "error.dateOfBirth." + (if (required) { "required" }
                                else { "invalid-format" })
      ))
        .foldLeft(form.discardingErrors)((form, error) => form.withError(error))
    } else {
      form
    }

}
