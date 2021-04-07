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
import java.util.Locale

import uk.gov.hmrc.homeofficesettledstatus.views.DateFormat.formatDatePattern
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

class DateFormatSpec extends WordSpecLike with Matchers with OptionValues {

  "DateFormat" should {

    "format a date" in {
      DateFormat.format(Locale.UK)(LocalDate.parse("1972-12-23")) shouldBe "23 December 1972"
      DateFormat.format(Locale.ITALIAN)(LocalDate.parse("1972-12-23")) shouldBe "23 dicembre 1972"
    }

    "format a date pattern string" in {
      formatDatePattern(Locale.UK)("1935-01-01") shouldBe "01 January 1935"
      formatDatePattern(Locale.ITALIAN)("1935-01-01") shouldBe "01 gennaio 1935"
      formatDatePattern(Locale.UK)("2001-12-13") shouldBe "13 December 2001"
      formatDatePattern(Locale.ITALIAN)("2001-12-13") shouldBe "13 dicembre 2001"
      formatDatePattern(Locale.UK)("2001-09-XX") shouldBe "September 2001"
      formatDatePattern(Locale.ITALIAN)("2001-09-XX") shouldBe "settembre 2001"
      formatDatePattern(Locale.UK)("2001-XX-XX") shouldBe "2001"
      formatDatePattern(Locale.ITALIAN)("2001-XX-XX") shouldBe "2001"
    }

  }
}
