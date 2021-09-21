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

package uk.gov.hmrc.homeofficesettledstatus.views

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import uk.gov.hmrc.homeofficesettledstatus.controllers.DateFieldHelper

object DateFormat {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  val yearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy")
  val yearMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

  def format(locale: Locale)(date: LocalDate): String =
    date.format(formatter.withLocale(locale))

  def formatDatePattern(locale: Locale)(datePattern: String): String =
    DateFieldHelper
      .parseDateIntoFields(datePattern)
      .map {
        case (year, month, day) =>
          if (day.isEmpty) {
            if (month.isEmpty)
              yearFormatter.withLocale(locale).format(LocalDate.parse(s"$year-01-01"))
            else
              yearMonthFormatter
                .withLocale(locale)
                .format(LocalDate.parse(s"$year-$month-01"))
          } else {
            formatter.withLocale(locale).format(LocalDate.parse(s"$year-$month-$day"))
          }
      }
      .getOrElse("")

}
