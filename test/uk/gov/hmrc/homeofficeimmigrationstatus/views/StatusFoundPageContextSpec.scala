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

import org.mockito.Mockito.{RETURNS_DEEP_STUBS, mock, never, reset, times, verify, when}
import org.mockito.ArgumentMatchers.{any, matches}
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDate
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Call
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficeimmigrationstatus.models.{ImmigrationStatus, StatusCheckByNinoRequest, StatusCheckResult}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

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
    withClue("msg key not defined in messages file") {
      realMessages(key) should not be key
    }

  //todo nino generator
  val query = StatusCheckByNinoRequest(Nino("RJ301829A"), "Surname", "Forename", "some dob")
  val call = Call("GET", "/")

  "currentStatusLabel" when {
    def createContext(pt: String, is: String, endDate: Option[LocalDate]) =
      StatusFoundPageContext(
        query,
        StatusCheckResult(
          fullName = "Some name",
          dateOfBirth = LocalDate.now,
          nationality = "Some nationality",
          statuses = List(ImmigrationStatus(LocalDate.MIN, endDate, pt, is, noRecourseToPublicFunds = true))
        ),
        call
      )

    Seq(
      ("EUS", "ILR", "status-found.current.EUS.ILR"),
      ("EUS", "LTR", "status-found.current.EUS.LTR"),
      ("non EUS", "LTR", "status-found.current.nonEUS.LTR"),
      ("non EUS", "ILR", "status-found.current.nonEUS.ILR"),
      ("non EUS", "LTE", "status-found.current.nonEUS.LTE"),
      ("EUS", "COA_IN_TIME_GRANT", "status-found.current.EUS.COA_IN_TIME_GRANT"),
      ("EUS", "POST_GRACE_PERIOD_COA_GRANT", "status-found.current.EUS.POST_GRACE_PERIOD_COA_GRANT"),
      ("FRONTIER WORKER", "PERMIT", "status-found.current.FRONTIER_WORKER.PERMIT")
    ).foreach {
      case (productType, immigrationStatus, msgKey) =>
        s"productType is $productType and immigrationStatus is $immigrationStatus" should {

          "give correct expired info" in {
            val date = LocalDate.now().minusDays(1)
            val sut = createContext(productType, immigrationStatus, Some(date))

            sut.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
            val msgKeyExpired = s"$msgKey.expired"
            verify(mockMessages, times(1)).apply(msgKeyExpired)
            checkMessagesFile(msgKeyExpired)
          }

          "give correct in-time info" in {
            val date = LocalDate.now()
            val sut = createContext(productType, immigrationStatus, Some(date))

            sut.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
            verify(mockMessages, times(1)).apply(msgKey)
            checkMessagesFile(msgKey)
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
    "return the results most recent" in {
      val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
      val fakeImmigrationStatus = ImmigrationStatus(LocalDate.now(), None, "TEST", "STATUS", true)
      when(mockResult.previousStatuses).thenReturn(Seq(fakeImmigrationStatus))

      StatusFoundPageContext(null, mockResult, null).previousStatuses shouldBe Seq(fakeImmigrationStatus)
    }
  }

  "show status label" should {
    implicit val messages: Messages =
      MessagesImpl(
        Lang("en-UK"),
        new DefaultMessagesApi(Map("en-UK" -> Map("app.status.EUS_LTR" -> "foo123", "app.status.EUS_ILR" -> "bar456"))))
    "work for EUS-LTR" in {
      StatusFoundPageContext.immigrationStatusLabel("EUS", "LTR") shouldBe "foo123"
    }
    "work for EUS-ILR" in {
      StatusFoundPageContext.immigrationStatusLabel("EUS", "ILR") shouldBe "bar456"
    }
    "work for unknown" in {
      StatusFoundPageContext.immigrationStatusLabel("FOO", "BAR") shouldBe "FOO + BAR"
    }
  }
}