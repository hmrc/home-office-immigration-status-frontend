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

package uk.gov.hmrc.homeofficeimmigrationstatus.views

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDate
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import play.api.mvc.Call
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}
import org.scalatest.OptionValues

class StatusFoundPageContextSpec extends AnyWordSpecLike with Matchers with OptionValues {

  val query = StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")
  val call = Call("GET", "/foo", "")

  val ILR = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2019-12-12"),
    statusEndDate = None,
    productType = "EUS",
    immigrationStatus = "ILR",
    noRecourseToPublicFunds = true
  )

  val LTR = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2018-07-13"),
    statusEndDate = Some(LocalDate.parse("2222-12-11")),
    productType = "EUS",
    immigrationStatus = "LTR",
    noRecourseToPublicFunds = true
  )

  val ILR_EXPIRED = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2019-12-12"),
    statusEndDate = Some(LocalDate.parse("2020-03-12")),
    productType = "EUS",
    immigrationStatus = "ILR",
    noRecourseToPublicFunds = true
  )

  val LTR_EXPIRED = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2018-07-13"),
    statusEndDate = Some(LocalDate.parse("2019-12-11")),
    productType = "EUS",
    immigrationStatus = "LTR",
    noRecourseToPublicFunds = true
  )

  val FOO = ImmigrationStatus(
    statusStartDate = LocalDate.parse("2015-11-11"),
    statusEndDate = Some(LocalDate.parse("2018-01-20")),
    productType = "FOO",
    immigrationStatus = "FOO",
    noRecourseToPublicFunds = false
  )

  "StatusFoundPageContext" should {
    "return correct status info when single ILR" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = List(ILR)
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe true
      context.hasExpiredImmigrationStatus shouldBe false
      context.mostRecentStatus shouldBe Some(ILR)
      context.previousStatuses shouldBe Nil
      context.statusClass shouldBe "success"
    }

    "return correct status info when single LTR" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = List(LTR)
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe true
      context.hasExpiredImmigrationStatus shouldBe false
      context.mostRecentStatus shouldBe Some(LTR)
      context.previousStatuses shouldBe Nil
      context.statusClass shouldBe "success"
    }

    "return correct status info when single expired ILR" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = List(ILR_EXPIRED)
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe true
      context.hasExpiredImmigrationStatus shouldBe true
      context.mostRecentStatus shouldBe Some(ILR_EXPIRED)
      context.previousStatuses shouldBe Nil
      context.statusClass shouldBe "success"
    }

    "return correct status info when single expired LTR" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = List(LTR_EXPIRED)
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe true
      context.hasExpiredImmigrationStatus shouldBe true
      context.mostRecentStatus shouldBe Some(LTR_EXPIRED)
      context.previousStatuses shouldBe Nil
      context.statusClass shouldBe "success"
    }

    "return correct status info when none" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = Nil
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe false
      context.hasExpiredImmigrationStatus shouldBe false
      context.mostRecentStatus shouldBe None
      context.previousStatuses shouldBe Nil
      context.statusClass shouldBe "error"
    }

    "return correct status info when non-standard" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = List(FOO)
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe false
      context.hasExpiredImmigrationStatus shouldBe false
      context.mostRecentStatus shouldBe Some(FOO)
      context.previousStatuses shouldBe Nil
      context.statusClass shouldBe "error"
    }

    "return correct status info when both ILR and LTR" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = List(FOO, ILR, LTR_EXPIRED)
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe true
      context.hasExpiredImmigrationStatus shouldBe false
      context.mostRecentStatus shouldBe Some(ILR)
      context.previousStatuses shouldBe Seq(LTR_EXPIRED, FOO)
      context.statusClass shouldBe "success"
    }

    "return correct status info when both ILR and LTR in reverse order" in {
      val result = StatusCheckResult(
        fullName = "Jane Doe",
        dateOfBirth = LocalDate.parse("2001-01-31"),
        nationality = "IRL",
        statuses = List(LTR_EXPIRED, FOO, ILR)
      )
      val context = StatusFoundPageContext(query, result, call)
      context.hasImmigrationStatus shouldBe true
      context.hasExpiredImmigrationStatus shouldBe false
      context.mostRecentStatus shouldBe Some(ILR)
      context.previousStatuses shouldBe Seq(LTR_EXPIRED, FOO)
      context.statusClass shouldBe "success"
    }

    "show status label" in {
      implicit val messages: Messages =
        MessagesImpl(
          Lang("en-UK"),
          new DefaultMessagesApi(
            Map("en-UK" -> Map("app.status.EUS_LTR" -> "foo123", "app.status.EUS_ILR" -> "bar456"))))
      StatusFoundPageContext.immigrationStatusLabel("EUS", "LTR") shouldBe "foo123"
      StatusFoundPageContext.immigrationStatusLabel("EUS", "ILR") shouldBe "bar456"
      StatusFoundPageContext.immigrationStatusLabel("FOO", "BAR") shouldBe "FOO + BAR"
    }

  }
}
