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

import play.api.data.Forms.{mapping, of}
import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation.Constraint

import scala.annotation.tailrec
import scala.util.Try
import scala.util.control.NonFatal

object DateFieldHelper {

  def validateDate(value: String): Boolean = {
    val parts = value.split("-")
    parts.size == 3 && isValidYear(parts(0)) && isValidMonth(parts(1)) && isValidDay(
      parts(2),
      toInt(parts(1)),
      toInt(parts(0)))
  }

  def isValidYear(year: String) = year.matches("""^\d\d\d\d$""") && toInt(year) >= 1900

  def isValidMonth(month: String) =
    if (month.contains("X")) month == "XX" else isInRange(toInt(month), 1, 12)

  def isValidDay(day: String, month: Int, year: Int) =
    if (day.contains("X")) day == "XX"
    else
      month match {
        case 4 | 6 | 9 | 11 => isInRange(toInt(day), 1, 30)
        case 2              => isInRange(toInt(day), 1, if (isLeapYear(year)) 29 else 28)
        case _              => isInRange(toInt(day), 1, 31)
      }

  def isLeapYear(year: Int): Boolean =
    (year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0)

  @tailrec
  def toInt(s: String): Int =
    if (s.startsWith("0")) toInt(s.drop(1)) else Try(s.toInt).toOption.getOrElse(-1)

  def isInRange(value: Int, minInc: Int, maxInc: Int): Boolean = value >= minInc && value <= maxInc

  def parseDateIntoFields(date: String): Option[(String, String, String)] =
    try {
      val ydm: Array[String] = date.split('-') ++ Array("", "")
      if (ydm.length >= 3) Some((ydm(0), removeWildcard(ydm(1)), removeWildcard(ydm(2)))) else None
    } catch {
      case NonFatal(_) => None
    }

  def removeWildcard(s: String): String = if (s.toUpperCase == "XX") "" else s

  val formatDateFromFields: (String, String, String) => String = {
    case (y, m, d) =>
      if (y.isEmpty) ""
      else {
        val year = if (y.length == 2) "19" + y else y
        val month = if (m.isEmpty) "XX" else if (m.length == 1) "0" + m else m
        val day = if (d.isEmpty) "XX" else if (d.length == 1) "0" + d else d
        s"$year-$month-$day"
      }
  }

  val validDobDateFormat: Constraint[String] =
    ValidateHelper
      .validateField("error.dateOfBirth.required", "error.dateOfBirth.invalid-format")(date =>
        validateDate(date))

  def dateFieldsMapping(constraintDate: Constraint[String]): Mapping[String] =
    mapping(
      "year"  -> of[String],
      "month" -> of[String].transform[String](_.toUpperCase, identity),
      "day"   -> of[String].transform[String](_.toUpperCase, identity)
    )(formatDateFromFields)(parseDateIntoFields).verifying(constraintDate)

}
