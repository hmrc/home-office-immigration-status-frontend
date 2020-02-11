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

import uk.gov.hmrc.homeofficesettledstatus.controllers.DateFieldHelper.{formatDateFromFields, parseDateIntoFields, validateDate}
import uk.gov.hmrc.play.test.UnitSpec

class DateFieldHelperSpec extends UnitSpec {

  "DateFieldHelper" should {
    "reject invalid date string" in {
      validateDate("") shouldBe false
      validateDate("   ") shouldBe false
      validateDate("   -  -  ") shouldBe false
      validateDate("0000-00-00") shouldBe false
      validateDate("0") shouldBe false
      validateDate("01") shouldBe false
      validateDate("01-") shouldBe false
      validateDate("---") shouldBe false
      validateDate("--") shouldBe false
      validateDate("-") shouldBe false
      validateDate("01-01-01") shouldBe false
      validateDate("2001-12-32") shouldBe false
      validateDate("2001--1-30") shouldBe false
      validateDate("2001-13-31") shouldBe false
      validateDate("2001-02-30") shouldBe false
      validateDate("2019-02-29") shouldBe false
      validateDate("201-01-01") shouldBe false
      validateDate("1899-01-01") shouldBe false
      validateDate("2222-0X-07") shouldBe false
      validateDate("2222-X1-07") shouldBe false
      validateDate("2222-XX-X2") shouldBe false
      validateDate("2222-XX-1X") shouldBe false
      validateDate("1900-12-2X") shouldBe false
      validateDate("1900-12-X2") shouldBe false
      validateDate("2222-XX-07") shouldBe false
    }
    "accept valid date string" in {
      validateDate("1970-01-01") shouldBe true
      validateDate("2001-12-31") shouldBe true
      validateDate("2999-06-07") shouldBe true
      validateDate("2222-XX-XX") shouldBe true
      validateDate("1900-12-XX") shouldBe true
    }
    "format date from fields" in {
      formatDateFromFields("", "", "") shouldBe ""
      formatDateFromFields("2019", "", "") shouldBe "2019-XX-XX"
      formatDateFromFields("96", "", "") shouldBe "1996-XX-XX"
      formatDateFromFields("2019", "05", "") shouldBe "2019-05-XX"
      formatDateFromFields("2019", "", "17") shouldBe "2019-XX-17"
      formatDateFromFields("2019", "7", "5") shouldBe "2019-07-05"
      formatDateFromFields("2019", "7", "") shouldBe "2019-07-XX"
      formatDateFromFields("2019", "", "1") shouldBe "2019-XX-01"
      formatDateFromFields("", "11", "30") shouldBe ""
    }
    "parse date into fields" in {
      parseDateIntoFields("2019-01-17") shouldBe Some("2019", "01", "17")
      parseDateIntoFields("2019") shouldBe Some("2019", "", "")
      parseDateIntoFields("2019-01") shouldBe Some("2019", "01", "")
      parseDateIntoFields("2019-01-XX") shouldBe Some("2019", "01", "")
      parseDateIntoFields("2019-XX-XX") shouldBe Some("2019", "", "")
      parseDateIntoFields("2019-XX-31") shouldBe Some("2019", "", "31")
      parseDateIntoFields("foo") shouldBe Some(("foo", "", ""))
      parseDateIntoFields("2019-foo-bar") shouldBe Some(("2019", "foo", "bar"))
    }
  }

}
