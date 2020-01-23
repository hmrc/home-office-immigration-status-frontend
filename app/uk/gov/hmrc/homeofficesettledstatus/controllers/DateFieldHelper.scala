/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatus.controllers

import play.api.data.Forms.{mapping, text}
import play.api.data.Mapping
import play.api.data.validation.Constraint

import scala.util.control.NonFatal

object DateFieldHelper {

  def validateDate(value: String): Boolean =
    if (value.matches("""^(1|2)(0|9)[0-9][0-9]-[0-1X][1-9X]-[0-3X][0-9X]$""")) true else false

  def parseDateIntoFields(date: String): Option[(String, String, String)] =
    try {
      val ydm: Array[String] = date.split("-")
      if (ydm.length >= 3) Some((ydm(0), ydm(1), ydm(2))) else None
    } catch {
      case NonFatal(_) => None
    }

  val formatDateFromFields: (String, String, String) => String = {
    case (y, m, d) =>
      if (y.isEmpty || m.isEmpty || d.isEmpty) ""
      else {
        val year = if (y.length == 2) "19" + y else y
        val month = if (m.length == 1) "0" + m else m
        val day = if (d.length == 1) "0" + d else d
        s"$year-$month-$day"
      }
  }

  val validDobDateFormat: Constraint[String] =
    ValidateHelper
      .validateField("error.dateOfBirth.required", "error.dateOfBirth.invalid-format")(date =>
        validateDate(date))

  def dateFieldsMapping(constraintDate: Constraint[String]): Mapping[String] =
    mapping(
      "year" -> text,
      //.verifying("error.year.invalid-format", y => y.nonEmpty && y.matches("^[0-9]{2,4}$")),
      "month" -> text,
      //.verifying("error.month.invalid-format", m => m.nonEmpty && m.matches("^[0-9X]{1,2}$")),
      "day" -> text
      //.verifying("error.day.invalid-format", d => d.nonEmpty && d.matches("^[0-9X]{1,2}$"))
    )(formatDateFromFields)(parseDateIntoFields).verifying(constraintDate)

}
