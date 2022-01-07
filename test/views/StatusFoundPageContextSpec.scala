/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import models.{EEACountries, ImmigrationStatus, MrzSearchFormModel, NinoSearchFormModel, StatusCheckResult}
import org.mockito.ArgumentMatchers.{any, matches}
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n._
import play.api.mvc.Call
import utils.NinoGenerator
import viewmodels.RowViewModel
import java.time.LocalDate

import config.Countries
import play.api.test.Injecting

class StatusFoundPageContextSpec
    extends AnyWordSpecLike with Matchers with OptionValues with BeforeAndAfterEach with GuiceOneAppPerSuite
    with Injecting {

  lazy val realMessages: Messages = inject[MessagesApi].preferred(Seq.empty)
  lazy val countries = inject[Countries]
  val allCountries: Countries = inject[Countries]

  val mockMessages: Messages = mock(classOf[MessagesImpl], RETURNS_DEEP_STUBS)
  val currentStatusLabelMsg = "current status label msg"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessages)
    when(mockMessages(matches("status-found\\.current.*"), any())).thenReturn(currentStatusLabelMsg)
  }

  def checkMessagesFile(key: String) =
    withClue(s"msg key:[$key] not defined in messages file") {
      assert(realMessages.isDefinedAt(key))
    }

  val ninoQuery = NinoSearchFormModel(NinoGenerator.generateNino, "Surname", "Forename", LocalDate.now())
  val mrzQuery = MrzSearchFormModel("PASSPORT", "123456", LocalDate.of(2001, 1, 31), "USA")
  val call = Call("GET", "/")

  def createNinoContext(pt: String, is: String, endDate: Option[LocalDate], hasRecourseToPublicFunds: Boolean = false) =
    StatusFoundPageContext(
      ninoQuery,
      StatusCheckResult(
        fullName = "Some name",
        dateOfBirth = LocalDate.now,
        nationality = "Some nationality",
        statuses = List(ImmigrationStatus(LocalDate.MIN, endDate, pt, is, hasRecourseToPublicFunds))
      )
    )

  def createMrzContext(pt: String, is: String, endDate: Option[LocalDate], hasRecourseToPublicFunds: Boolean = false) =
    StatusFoundPageContext(
      mrzQuery,
      StatusCheckResult(
        fullName = "Some name",
        dateOfBirth = LocalDate.now,
        nationality = "Some nationality",
        statuses = List(ImmigrationStatus(LocalDate.MIN, endDate, pt, is, hasRecourseToPublicFunds))
      )
    )

  "currentStatusLabel" when {
    Seq(
      ("EUS", "ILR"),
      ("EUS", "LTR"),
      ("EUS", "COA_IN_TIME_GRANT"),
      ("EUS", "POST_GRACE_PERIOD_COA_GRANT")
    ).foreach {
      case (productType, immigrationStatus) =>
        s"productType is EUS and immigrationStatus is $immigrationStatus" should {
          "give correct in-time info" in {
            val msgKey = s"status-found.current.$productType.$immigrationStatus"
            when(mockMessages.isDefinedAt(any())).thenReturn(true)
            val date = LocalDate.now()
            val sut = createNinoContext(productType, immigrationStatus, Some(date))

            sut.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
            verify(mockMessages, times(1)).apply(msgKey)
            checkMessagesFile(msgKey)
          }
        }
    }

    Seq(
      ("non EUS", "LTR"),
      ("other", "ILR"),
      ("non EUS", "LTE"),
      ("FRONTIER_WORKER", "PERMIT")
    ).foreach {
      case (productType, immigrationStatus) =>
        s"productType is non EUS and immigrationStatus is $immigrationStatus" should {
          "give correct in-time info" in {
            val msgKey = s"status-found.current.nonEUS.$immigrationStatus"
            when(mockMessages.isDefinedAt(any())).thenReturn(true)
            val date = LocalDate.now()
            val sut = createNinoContext(productType, immigrationStatus, Some(date))

            sut.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
            verify(mockMessages, times(1)).apply(msgKey)
            checkMessagesFile(msgKey)
          }
        }
    }
    Seq(
      ("EUS", "ILR"),
      ("EUS", "LTR"),
      ("EUS", "COA_IN_TIME_GRANT"),
      ("EUS", "POST_GRACE_PERIOD_COA_GRANT")
    ).foreach {
      case (productType, immigrationStatus) =>
        s"productType is EUS and immigrationStatus is $immigrationStatus and is expired" should {
          "give correct expired info" in {
            when(mockMessages.isDefinedAt(any())).thenReturn(true)
            val date = LocalDate.now().minusDays(1)
            val sut = createNinoContext(productType, immigrationStatus, Some(date))

            sut.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
            val msgKeyExpired = s"status-found.current.EUS.$immigrationStatus.expired"
            verify(mockMessages, times(1)).apply(msgKeyExpired)
            checkMessagesFile(msgKeyExpired)
          }
        }
    }

    Seq(
      ("non EUS", "LTR"),
      ("some", "ILR"),
      ("other", "LTE"),
      ("FRONTIER_WORKER", "PERMIT")
    ).foreach {
      case (productType, immigrationStatus) =>
        s"productType is $productType and immigrationStatus is $immigrationStatus and is expired" should {
          "give correct expired info" in {
            when(mockMessages.isDefinedAt(any())).thenReturn(true)
            val date = LocalDate.now().minusDays(1)
            val sut = createNinoContext(productType, immigrationStatus, Some(date))

            sut.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
            val msgKeyExpired = s"status-found.current.nonEUS.$immigrationStatus.expired"
            verify(mockMessages, times(1)).apply(msgKeyExpired)
            checkMessagesFile(msgKeyExpired)
          }
        }
    }

    "the immigration status is unrecognised" should {
      "provide a temporary description" in {
        val context = createNinoContext("FOO", "BAR", None)
        val msgKey = "status-found.current.hasFBIS"

        context.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
        checkMessagesFile(msgKey)
      }
    }

    "there is no immigration Status" should {
      "display no status" in {
        val context =
          StatusFoundPageContext(
            ninoQuery,
            StatusCheckResult("Some name", LocalDate.MIN, "some nation", statuses = Nil))

        context.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
        val msgKey = "status-found.current.noStatus"
        verify(mockMessages, times(1)).apply(msgKey)
        checkMessagesFile(msgKey)
      }
    }
  }

  "mostRecentStatus" should {
    "return the results most recent" in {
      val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
      val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "TEST", "STATUS", true)
      when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))

      StatusFoundPageContext(null, mockResult).mostRecentStatus shouldBe Some(fakeImmigrationStatus)
    }
  }

  "previousStatuses" should {
    "return previous statuses" in {
      val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
      val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "TEST", "STATUS", true)
      when(mockResult.previousStatuses).thenReturn(Seq(fakeImmigrationStatus))
      when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
      when(mockResult.nationality).thenReturn("FRA")

      StatusFoundPageContext(null, mockResult).previousStatuses shouldBe Seq(fakeImmigrationStatus)
    }
  }

  "displayNoResourceToPublicFunds" should {
    "return false when noRecourseToPublicFunds is true" in {
      val context = createNinoContext("FOO", "BAR", None, true)
      assert(context.hasRecourseToPublicFunds == false)
    }

    "return true" when {
      "most recent is none" in {
        val context = StatusFoundPageContext(
          ninoQuery,
          StatusCheckResult(
            fullName = "Some name",
            dateOfBirth = LocalDate.now,
            nationality = "Some nationality",
            statuses = Nil
          )
        )

        assert(context.hasRecourseToPublicFunds == true)
      }

      "noRecourseToPublicFunds is false" in {
        val context = createNinoContext("FOO", "BAR", None, false)
        assert(context.hasRecourseToPublicFunds == true)
      }
    }
  }

  "detailRows" must {
    "populate the row objects correctly" when {
      val context = createNinoContext("PT", "IS", Some(LocalDate.now()))
      Seq(
        ("nino", "generic.nino", ninoQuery.nino.nino),
        ("nationality", "generic.nationality", countries.getCountryNameFor(context.result.nationality)),
        ("dob", "generic.dob", context.result.dobFormatted(realMessages.lang.locale))
      ).foreach {
        case (id, msgKey, data) =>
          s"it's a NINO search and the row is $id" in {
            assert(context.detailRows(countries)(realMessages).contains(RowViewModel(id, msgKey, data)))
          }
      }

      val mrzContext = createMrzContext("PT", "IS", Some(LocalDate.now()))
      Seq(
        (
          "documentType",
          "lookup.identity.label",
          MrzSearchFormModel.documentTypeToMessageKey(mrzQuery.documentType)(realMessages)),
        ("documentNumber", "lookup.mrz.label", mrzQuery.documentNumber),
        ("nationality", "generic.nationality", countries.getCountryNameFor(mrzContext.result.nationality)),
        ("dob", "generic.dob", mrzContext.result.dobFormatted(realMessages.lang.locale))
      ).foreach {
        case (id, msgKey, data) =>
          s"it's a mrz search and the row is $id" in {
            assert(mrzContext.detailRows(countries)(realMessages).contains(RowViewModel(id, msgKey, data)))
          }
      }
    }
  }

  "immigrationStatusRows" must {
    "populate the row objects correctly" when {
      val context = createNinoContext("PT", "IS", Some(LocalDate.now()))
      Seq(
        ("immigrationRoute", "status-found.route", context.immigrationRoute(realMessages).get),
        (
          "startDate",
          "status-found.startDate",
          DateFormat.format(realMessages.lang.locale)(context.mostRecentStatus.get.statusStartDate)),
        (
          "expiryDate",
          "status-found.endDate",
          DateFormat.format(realMessages.lang.locale)(context.mostRecentStatus.get.statusEndDate.get))
      ).foreach {
        case (id, msgKey, data) =>
          s"row is for $id" in {
            assert(context.immigrationStatusRows(realMessages).contains(RowViewModel(id, msgKey, data)))
          }
      }
    }
  }

  "isZambrano" should {

    val nonEEACountries = allCountries.countries.filter(c => !EEACountries.countries.contains(c.alpha3))

    "return false" when {
      "the product type is EUS and the nationality is an EEA country" in {
        EEACountries.countries.foreach { country =>
          val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
          val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "EUS", "STATUS", true)
          when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
          when(mockResult.nationality).thenReturn(country)

          StatusFoundPageContext(null, mockResult).isZambrano shouldBe false
        }
      }

      "the product type is NOT EUS and the nationality is an EEA country" in {
        EEACountries.countries.foreach { country =>
          val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
          val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "WORK", "STATUS", true)
          when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
          when(mockResult.nationality).thenReturn(country)

          StatusFoundPageContext(null, mockResult).isZambrano shouldBe false
        }
      }

      "the product type is NOT EUS and the nationality is a non EEA country" in {
        nonEEACountries.foreach { country =>
          val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
          val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "WORK", "STATUS", true)
          when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
          when(mockResult.nationality).thenReturn(country.alpha3)

          StatusFoundPageContext(null, mockResult).isZambrano shouldBe false
        }
      }

    }

    "return true" when {
      "the product type is EUS and the nationality is a non EEA country" in {
        nonEEACountries.foreach { country =>
          val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
          val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "EUS", "STATUS", true)
          when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
          when(mockResult.nationality).thenReturn(country.alpha3)

          StatusFoundPageContext(null, mockResult).isZambrano shouldBe true
        }
      }
    }
  }

  "immigrationStatusLabel" should {
    implicit val messages: Messages = realMessages

    def createStatus(productType: String, immigrationStatus: String): ImmigrationStatus = ImmigrationStatus(
      statusStartDate = LocalDate.now,
      statusEndDate = None,
      productType = productType,
      immigrationStatus = immigrationStatus,
      noRecourseToPublicFunds = true
    )

    Seq(
      ("EUS", "ILR", "EU Settlement Scheme - Settled status"),
      ("EUS", "LTR", "EU Settlement Scheme - Pre-settled status"),
      ("STUDY", "LTE", "Student - Limited leave to enter"),
      ("STUDY", "LTR", "Student - Limited leave to remain"),
      ("DEPENDANT", "LTE", "Settlement Protection - Limited leave to enter"),
      ("DEPENDANT", "LTR", "Settlement Protection - Limited leave to remain"),
      ("WORK", "LTE", "Worker - Limited leave to enter"),
      ("WORK", "LTR", "Worker - Limited leave to remain"),
      ("FRONTIER_WORKER", "PERMIT", "Frontier worker - Frontier worker permit"),
      ("BNO", "LTE", "British National Overseas - Limited leave to enter"),
      ("BNO", "LTR", "British National Overseas - Limited leave to remain"),
      ("BNO_LOTR", "LTE", "British National Overseas (leave outside the rules) - Limited leave to enter"),
      ("BNO_LOTR", "LTR", "British National Overseas (leave outside the rules) - Limited leave to remain"),
      ("GRADUATE", "LTR", "Graduate - Limited leave to remain"),
      ("EUS", "COA_IN_TIME_GRANT", "EU Settlement Scheme - Pending EU Settlement Scheme application"),
      ("EUS", "POST_GRACE_PERIOD_COA_GRANT", "EU Settlement Scheme - Pending EU Settlement Scheme application"),
      ("SPORTSPERSON", "LTR", "International Sportsperson - Limited leave to remain"),
      ("SPORTSPERSON", "LTE", "International Sportsperson - Limited leave to enter"),
      ("SETTLEMENT", "ILR", "British National Overseas or Settlement Protection - Indefinite leave to remain"),
      ("TEMP_WORKER", "LTR", "Temporary Worker - Limited leave to remain"),
      ("TEMP_WORKER", "LTE", "Temporary Worker - Limited leave to enter"),
      ("EUS_EUN_JFM", "ILR", "EU Settlement Scheme (joiner family member) - Settled status"),
      ("EUS_EUN_JFM", "LTR", "EU Settlement Scheme (joiner family member) - Pre-settled status"),
      (
        "EUS_EUN_JFM",
        "COA_IN_TIME_GRANT",
        "EU Settlement Scheme (joiner family member) - Pending EU Settlement Scheme application"),
      ("EUS_TCN_JFM", "ILR", "EU Settlement Scheme (joiner family member) - Settled status"),
      ("EUS_TCN_JFM", "LTR", "EU Settlement Scheme (joiner family member) - Pre-settled status"),
      (
        "EUS_TCN_JFM",
        "COA_IN_TIME_GRANT",
        "EU Settlement Scheme (joiner family member) - Pending EU Settlement Scheme application"),
      ("EUS_TCNBRC_JFM", "ILR", "EU Settlement Scheme (joiner family member) - Settled status"),
      ("EUS_TCNBRC_JFM", "LTR", "EU Settlement Scheme (joiner family member) - Pre-settled status"),
      (
        "EUS_TCNBRC_JFM",
        "COA_IN_TIME_GRANT",
        "EU Settlement Scheme (joiner family member) - Pending EU Settlement Scheme application"),
      ("EUS_JFM", "ILR", "EU Settlement Scheme (joiner family member) - Settled status"),
      ("EUS_JFM", "LTR", "EU Settlement Scheme (joiner family member) - Pre-settled status"),
      (
        "EUS_JFM",
        "COA_IN_TIME_GRANT",
        "EU Settlement Scheme (joiner family member) - Pending EU Settlement Scheme application"),
      ("EUS_FMFW", "ILR", "EU Settlement Scheme (frontier worker family member) - Settled status"),
      ("EUS_FMFW", "LTR", "EU Settlement Scheme (frontier worker family member) - Pre-settled status"),
      (
        "EUS_FMFW",
        "COA_IN_TIME_GRANT",
        "EU Settlement Scheme (frontier worker family member) - Pending EU Settlement Scheme application"),
      ("PROTECTION", "LTR", "Settlement Protection - Limited leave to remain"),
      ("PROTECTION_ROUTE", "LTR", "Settlement Protection - Limited leave to remain"),
      ("DEPENDANT", "ILR", "Settlement Protection - Indefinite leave to remain"),
      (
        "DEPENDANT_ACRS_PARTNER_LOTR",
        "ILR",
        "Afghan Citizens Resettlement Scheme (partner) - Indefinite leave to remain"),
      ("DEPENDANT_ACRS_PARTNER_LOTR", "LTR", "Afghan Citizens Resettlement Scheme (partner) - Limited leave to remain"),
      ("DEPENDANT_ACRS_CHILD", "ILR", "Afghan Citizens Resettlement Scheme (child) - Indefinite leave to remain"),
      ("DEPENDANT_ACRS_CHILD", "LTR", "Afghan Citizens Resettlement Scheme (child) - Limited leave to remain"),
      ("DEPENDANT_ACRS_CHILD_LOTR", "ILR", "Afghan Citizens Resettlement Scheme (child) - Indefinite leave to remain"),
      ("DEPENDANT_ACRS_CHILD_LOTR", "LTR", "Afghan Citizens Resettlement Scheme (child) - Limited leave to remain"),
      ("DEPENDANT_ACRS_OTHER", "ILR", "Afghan Citizens Resettlement Scheme (other) - Indefinite leave to remain"),
      ("DEPENDANT_ACRS_OTHER", "LTR", "Afghan Citizens Resettlement Scheme (other) - Limited leave to remain"),
      ("DEPENDANT_ACRS_OTHER_LOTR", "ILR", "Afghan Citizens Resettlement Scheme (other) - Indefinite leave to remain"),
      ("DEPENDANT_ACRS_OTHER_LOTR", "LTR", "Afghan Citizens Resettlement Scheme (other) - Limited leave to remain"),
      ("DEPENDANT_AOP_PARTNER", "ILR", "Afghan Operation Pitting (partner) - Indefinite leave to remain"),
      ("DEPENDANT_AOP_PARTNER", "LTR", "Afghan Operation Pitting (partner) - Limited leave to remain"),
      ("DEPENDANT_AOP_PARTNER_LOTR", "ILR", "Afghan Operation Pitting (partner) - Indefinite leave to remain"),
      ("DEPENDANT_AOP_PARTNER_LOTR", "LTR", "Afghan Operation Pitting (partner) - Limited leave to remain"),
      ("DEPENDANT_AOP_CHILD", "ILR", "Afghan Operation Pitting (child) - Indefinite leave to remain"),
      ("DEPENDANT_AOP_CHILD", "LTR", "Afghan Operation Pitting (child) - Limited leave to remain"),
      ("DEPENDANT_AOP_CHILD_LOTR", "ILR", "Afghan Operation Pitting (child) - Indefinite leave to remain"),
      ("DEPENDANT_AOP_CHILD_LOTR", "LTR", "Afghan Operation Pitting (child) - Limited leave to remain"),
      ("DEPENDANT_AOP_OTHER", "ILR", "Afghan Operation Pitting (other) - Indefinite leave to remain"),
      ("DEPENDANT_AOP_OTHER", "LTR", "Afghan Operation Pitting (other) - Limited leave to remain"),
      ("DEPENDANT_AOP_OTHER_LOTR", "ILR", "Afghan Operation Pitting (other) - Indefinite leave to remain"),
      ("DEPENDANT_AOP_OTHER_LOTR", "LTR", "Afghan Operation Pitting (other) - Limited leave to remain"),
      ("SETTLEMENT_ALES", "ILR", "Afghan Locally Engaged Staff - Indefinite leave to remain"),
      ("SETTLEMENT_ALES_LOTR", "ILR", "Afghan Locally Engaged Staff - Indefinite leave to remain"),
      ("SETTLEMENT_ACRS", "ILR", "Afghan Citizens Resettlement Scheme - Indefinite leave to remain"),
      ("SETTLEMENT_ACRS_LOTR", "ILR", "Afghan Citizens Resettlement Scheme - Indefinite leave to remain"),
      ("RESETTLEMENT_ALES", "LTR", "Afghan Locally Engaged Staff - Limited leave to remain"),
      ("RESETTLEMENT_ALES_LOTR", "LTR", "Afghan Locally Engaged Staff - Limited leave to remain"),
      ("RESETTLEMENT_ACRS", "LTR", "Afghan Citizens Resettlement Scheme - Limited leave to remain"),
      ("RESETTLEMENT_ACRS_LOTR", "LTR", "Afghan Citizens Resettlement Scheme - Limited leave to remain"),
      ("DEPENDANT_ALES_PARTNER", "ILR", "Afghan Locally Engaged Staff (partner) - Indefinite leave to remain"),
      ("DEPENDANT_ALES_PARTNER", "LTR", "Afghan Locally Engaged Staff (partner) - Limited leave to remain"),
      ("DEPENDANT_ALES_PARTNER_LOTR", "ILR", "Afghan Locally Engaged Staff (partner) - Indefinite leave to remain"),
      ("DEPENDANT_ALES_PARTNER_LOTR", "LTR", "Afghan Locally Engaged Staff (partner) - Limited leave to remain"),
      ("DEPENDANT_ALES_CHILD", "ILR", "Afghan Locally Engaged Staff (child) - Indefinite leave to remain"),
      ("DEPENDANT_ALES_CHILD", "LTR", "Afghan Locally Engaged Staff (child) - Limited leave to remain"),
      ("DEPENDANT_ALES_CHILD_LOTR", "ILR", "Afghan Locally Engaged Staff (child) - Indefinite leave to remain"),
      ("DEPENDANT_ALES_CHILD_LOTR", "LTR", "Afghan Locally Engaged Staff (child) - Limited leave to remain"),
      ("DEPENDANT_ALES_OTHER", "ILR", "Afghan Locally Engaged Staff (other) - Indefinite leave to remain"),
      ("DEPENDANT_ALES_OTHER", "LTR", "Afghan Locally Engaged Staff (other) - Limited leave to remain"),
      ("DEPENDANT_ALES_OTHER_LOTR", "ILR", "Afghan Locally Engaged Staff (other) - Indefinite leave to remain"),
      ("DEPENDANT_ALES_OTHER_LOTR", "LTR", "Afghan Locally Engaged Staff (other) - Limited leave to remain"),
      ("DEPENDANT_ACRS_PARTNER", "ILR", "Afghan Citizens Resettlement Scheme (partner) - Indefinite leave to remain"),
      ("DEPENDANT_ACRS_PARTNER", "LTR", "Afghan Citizens Resettlement Scheme (partner) - Limited leave to remain")
    ).foreach {
      case (product, status, label) =>
        s"format label for $product - $status" in {
          val fullStatus = createStatus(product, status)
          StatusFoundPageContext.immigrationStatusLabel(fullStatus) shouldBe label
        }
    }

    "return the product type label where that exists but status doesn't" in {
      val fullStatus = createStatus("EUS", "LTF")
      StatusFoundPageContext.immigrationStatusLabel(fullStatus) shouldBe "EU Settlement Scheme - LTF"
    }

    "return the status label where that exists but product type doesn't" in {
      val fullStatus = createStatus("NEW", "LTR")
      StatusFoundPageContext.immigrationStatusLabel(fullStatus) shouldBe "NEW - Limited leave to remain"
    }

    "return the status label where that exists but product type doesn't (but is EUS)" in {
      val fullStatus = createStatus("EUS_NEW", "LTR")
      StatusFoundPageContext.immigrationStatusLabel(fullStatus) shouldBe "EUS_NEW - Pre-settled status"
    }

    "return the HO codes where neither exist in messages" in {
      val fullStatus = createStatus("EUS_NEW", "LTF")
      StatusFoundPageContext.immigrationStatusLabel(fullStatus) shouldBe "EUS_NEW - LTF"
    }

  }

}
