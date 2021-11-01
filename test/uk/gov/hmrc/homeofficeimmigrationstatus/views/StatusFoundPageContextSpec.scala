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

import org.mockito.ArgumentMatchers.{any, matches}
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n._
import play.api.mvc.Call
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{ImmigrationStatus, StatusCheckByNinoFormModel, StatusCheckResult}
import uk.gov.hmrc.homeofficeimmigrationstatus.viewmodels.RowViewModel

import java.time.LocalDate

class StatusFoundPageContextSpec
    extends AnyWordSpecLike with Matchers with OptionValues with BeforeAndAfterEach with GuiceOneAppPerSuite {

  val realMessages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty[Lang])
  //todo mockito Sugar
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

  //todo nino generator
  val query = StatusCheckByNinoFormModel(Nino("RJ301829A"), "Surname", "Forename", "some dob")
  val call = Call("GET", "/")

  def createContext(pt: String, is: String, endDate: Option[LocalDate], hasRecourseToPublicFunds: Boolean = false) =
    StatusFoundPageContext(
      query,
      StatusCheckResult(
        fullName = "Some name",
        dateOfBirth = LocalDate.now,
        nationality = "Some nationality",
        statuses = List(ImmigrationStatus(LocalDate.MIN, endDate, pt, is, hasRecourseToPublicFunds))
      ),
      call
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
            val sut = createContext(productType, immigrationStatus, Some(date))

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
            val sut = createContext(productType, immigrationStatus, Some(date))

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
            val sut = createContext(productType, immigrationStatus, Some(date))

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
            val sut = createContext(productType, immigrationStatus, Some(date))

            sut.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
            val msgKeyExpired = s"status-found.current.nonEUS.$immigrationStatus.expired"
            verify(mockMessages, times(1)).apply(msgKeyExpired)
            checkMessagesFile(msgKeyExpired)
          }
        }
    }

    "the immigration status is unrecognised" should {
      "provide a temporary description" in {
        val context = createContext("FOO", "BAR", None)
        val msgKey = "status-found.current.hasFBIS"

        context.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
        checkMessagesFile(msgKey)
      }
    }

    "there is no immigration Status" should {
      "display no status" in {
        val context = StatusFoundPageContext(
          query,
          StatusCheckResult("Some name", LocalDate.MIN, "some nation", statuses = Nil),
          call)

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

      StatusFoundPageContext(null, mockResult, null).mostRecentStatus shouldBe Some(fakeImmigrationStatus)
    }
  }

  "previousStatuses" should {
    "return previous statuses" in {
      val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
      val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "TEST", "STATUS", true)
      when(mockResult.previousStatuses).thenReturn(Seq(fakeImmigrationStatus))

      StatusFoundPageContext(null, mockResult, null).previousStatuses shouldBe Seq(fakeImmigrationStatus)
    }
  }

  "displayNoResourceToPublicFunds" should {
    "return false when noRecourseToPublicFunds is true" in {
      val context = createContext("FOO", "BAR", None, true)
      assert(context.hasRecourseToPublicFunds == false)
    }

    "return true" when {
      "most recent is none" in {
        val context = StatusFoundPageContext(
          query,
          StatusCheckResult(
            fullName = "Some name",
            dateOfBirth = LocalDate.now,
            nationality = "Some nationality",
            statuses = Nil
          ),
          call
        )

        assert(context.hasRecourseToPublicFunds == true)
      }

      "noRecourseToPublicFunds is false" in {
        val context = createContext("FOO", "BAR", None, false)
        assert(context.hasRecourseToPublicFunds == true)
      }
    }
  }

  "detailRows" must {
    "populate the row objects correctly" when {
      val context = createContext("PT", "IS", Some(LocalDate.now()))
      Seq(
        ("nino", "generic.nino", query.nino.nino),
        ("dob", "generic.dob", context.result.dobFormatted(realMessages.lang.locale)),
        ("nationality", "generic.nationality", context.result.countryName)
      ).foreach {
        case (id, msgKey, data) =>
          s"row is for $id" in {
            assert(context.detailRows(realMessages).contains(RowViewModel(id, msgKey, data)))
          }
      }
    }
  }

  "immigrationStatusRows" must {
    "populate the row objects correctly" when {
      val context = createContext("PT", "IS", Some(LocalDate.now()))
      Seq(
        ("immigrationRoute", "status-found.route", context.immigrationRoute(realMessages).get),
        (
          "startDate",
          "status-found.startDate",
          DateFormat.format(realMessages.lang.locale)(context.mostRecentStatus.get.statusStartDate)),
        (
          "expiryDate",
          "status-found.endDate",
          DateFormat.format(realMessages.lang.locale)(context.mostRecentStatus.get.statusEndDate.get)),
        ("recourse-text", "status-found.norecourse", realMessages("status-found.yes"))
      ).foreach {
        case (id, msgKey, data) =>
          s"row is for $id" in {
            assert(context.immigrationStatusRows(realMessages).contains(RowViewModel(id, msgKey, data)))
          }
      }
    }
  }

  "show status label" should {
    implicit val messages: Messages =
      MessagesImpl(
        Lang("en-UK"),
        new DefaultMessagesApi(
          Map("en-UK" -> Map("immigration.eus.ltr" -> "foo123", "immigration.eus.ilr" -> "bar456"))))
    "work for EUS-LTR" in {
      StatusFoundPageContext.immigrationStatusLabel("EUS", "LTR") shouldBe "foo123"
    }
    "work for EUS-ILR" in {
      StatusFoundPageContext.immigrationStatusLabel("EUS", "ILR") shouldBe "bar456"
    }
    "work for unknown" in {
      StatusFoundPageContext.immigrationStatusLabel("FOO", "BAR") shouldBe "FOO - BAR"
    }
  }
}
