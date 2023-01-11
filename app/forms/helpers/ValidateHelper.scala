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

package forms.helpers

import play.api.data.validation.{Constraint, Constraints, Invalid, Valid, ValidationError}

object ValidateHelper extends Constraints {

  def cond[A](failure: String)(condition: A => Boolean): Constraint[A] =
    Constraint[A] { data: A =>
      if (condition(data)) {
        Valid
      } else {
        Invalid(ValidationError(failure))
      }
    }

  def validateField(emptyFailure: String, invalidFailure: String)(condition: String => Boolean): Constraint[String] =
    Constraint[String] { fieldValue: String =>
      val nonEmptyConstraint: Constraint[String] = nonEmpty(emptyFailure)
      nonEmptyConstraint(fieldValue) match {
        case i: Invalid =>
          i
        case Valid =>
          if (condition(fieldValue.trim.toUpperCase)) {
            Valid
          } else {
            Invalid(ValidationError(invalidFailure))
          }
      }
    }
}
