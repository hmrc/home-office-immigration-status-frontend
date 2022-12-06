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

import config.Countries
import models._
import org.mockito.ArgumentMatchers.{any, matches}
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{Assertion, BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.Injecting
import repositories.SessionCacheRepository
import utils.NinoGenerator
import viewmodels.RowViewModel

import java.time.LocalDate

class StatusFoundPageContextSpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite
    with Injecting {

  val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .build()

  lazy val realMessages: Messages = inject[MessagesApi].preferred(Seq.empty)
  lazy val countries: Countries   = inject[Countries]
  val allCountries: Countries     = inject[Countries]

  val mockMessages: Messages = mock(classOf[MessagesImpl], RETURNS_DEEP_STUBS)
  val currentStatusLabelMsg  = "current status label msg"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessages)
    when(mockMessages(matches("status-found\\.current.*"), any())).thenReturn(currentStatusLabelMsg)
  }

  def checkMessagesFile(key: String): Assertion =
    withClue(s"msg key:[$key] not defined in messages file") {
      assert(realMessages.isDefinedAt(key))
    }

  val ninoQuery: NinoSearchFormModel =
    NinoSearchFormModel(NinoGenerator.generateNino, "Surname", "Forename", LocalDate.now())
  val mrzQuery: MrzSearchFormModel =
    MrzSearchFormModel("PASSPORT", "123456", LocalDate.of(2001, 1, 31), "USA") //scalastyle:off magic.number
  val call: Call = Call("GET", "/")

  def createNinoContext(
    pt: String,
    is: String,
    endDate: Option[LocalDate],
    hasRecourseToPublicFunds: Boolean = false
  ): StatusFoundPageContext =
    StatusFoundPageContext(
      ninoQuery,
      StatusCheckResult(
        fullName = "Some name",
        dateOfBirth = LocalDate.now,
        nationality = "Some nationality",
        statuses = List(ImmigrationStatus(LocalDate.MIN, endDate, pt, is, hasRecourseToPublicFunds))
      )
    )

  def createMrzContext(
    pt: String,
    is: String,
    endDate: Option[LocalDate],
    hasRecourseToPublicFunds: Boolean = false
  ): StatusFoundPageContext =
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
    ).foreach { case (productType, immigrationStatus) =>
      s"productType is EUS and immigrationStatus is $immigrationStatus" should {
        "give correct in-time info" in {
          val msgKey = s"status-found.current.$productType.$immigrationStatus"
          when(mockMessages.isDefinedAt(any())).thenReturn(true)
          val date = LocalDate.now()
          val sut  = createNinoContext(productType, immigrationStatus, Some(date))

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
    ).foreach { case (productType, immigrationStatus) =>
      s"productType is non EUS and immigrationStatus is $immigrationStatus" should {
        "give correct in-time info" in {
          val msgKey = s"status-found.current.nonEUS.$immigrationStatus"
          when(mockMessages.isDefinedAt(any())).thenReturn(true)
          val date = LocalDate.now()
          val sut  = createNinoContext(productType, immigrationStatus, Some(date))

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
    ).foreach { case (productType, immigrationStatus) =>
      s"productType is EUS and immigrationStatus is $immigrationStatus and is expired" should {
        "give correct expired info" in {
          when(mockMessages.isDefinedAt(any())).thenReturn(true)
          val date = LocalDate.now().minusDays(1)
          val sut  = createNinoContext(productType, immigrationStatus, Some(date))

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
    ).foreach { case (productType, immigrationStatus) =>
      s"productType is $productType and immigrationStatus is $immigrationStatus and is expired" should {
        "give correct expired info" in {
          when(mockMessages.isDefinedAt(any())).thenReturn(true)
          val date = LocalDate.now().minusDays(1)
          val sut  = createNinoContext(productType, immigrationStatus, Some(date))

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
        val msgKey  = "status-found.current.hasFBIS"

        context.currentStatusLabel(mockMessages) shouldBe currentStatusLabelMsg
        checkMessagesFile(msgKey)
      }
    }

    "there is no immigration Status" should {
      "display no status" in {
        val context =
          StatusFoundPageContext(
            ninoQuery,
            StatusCheckResult("Some name", LocalDate.MIN, "some nation", statuses = Nil)
          )

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
      val fakeImmigrationStatus =
        ImmigrationStatus(LocalDate.now(), None, "TEST", "STATUS", noRecourseToPublicFunds = true)
      when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
      //scalastyle:off null
      StatusFoundPageContext(null, mockResult).mostRecentStatus shouldBe Some(fakeImmigrationStatus)

    }
  }

  "previousStatuses" should {
    "return previous statuses" in {
      val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
      val fakeImmigrationStatus =
        ImmigrationStatus(LocalDate.now(), None, "TEST", "STATUS", noRecourseToPublicFunds = true)
      when(mockResult.previousStatuses).thenReturn(Seq(fakeImmigrationStatus))
      when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
      when(mockResult.nationality).thenReturn("FRA")

      StatusFoundPageContext(null, mockResult).previousStatuses shouldBe Seq(fakeImmigrationStatus)
    }
  }

  "displayNoResourceToPublicFunds" should {
    "return false when noRecourseToPublicFunds is true" in {
      val context = createNinoContext("FOO", "BAR", None, hasRecourseToPublicFunds = true)
      assert(!context.hasRecourseToPublicFunds)
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

        assert(context.hasRecourseToPublicFunds)
      }

      "noRecourseToPublicFunds is false" in {
        val context = createNinoContext("FOO", "BAR", None)
        assert(context.hasRecourseToPublicFunds)
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
      ).foreach { case (id, msgKey, data) =>
        s"it's a NINO search and the row is $id" in {
          assert(context.detailRows(countries)(realMessages).contains(RowViewModel(id, msgKey, data)))
        }
      }

      val mrzContext = createMrzContext("PT", "IS", Some(LocalDate.now()))
      Seq(
        (
          "documentType",
          "lookup.identity.label",
          MrzSearch.documentTypeToMessageKey(mrzQuery.documentType)(realMessages)
        ),
        ("documentNumber", "lookup.mrz.label", mrzQuery.documentNumber),
        ("nationality", "generic.nationality", countries.getCountryNameFor(mrzContext.result.nationality)),
        ("dob", "generic.dob", mrzContext.result.dobFormatted(realMessages.lang.locale))
      ).foreach { case (id, msgKey, data) =>
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
          DateFormat.format(realMessages.lang.locale)(context.mostRecentStatus.get.statusStartDate)
        ),
        (
          "expiryDate",
          "status-found.endDate",
          DateFormat.format(realMessages.lang.locale)(context.mostRecentStatus.get.statusEndDate.get)
        )
      ).foreach { case (id, msgKey, data) =>
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
          val fakeImmigrationStatus =
            ImmigrationStatus(LocalDate.now(), None, "EUS", "STATUS", noRecourseToPublicFunds = true)
          when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
          when(mockResult.nationality).thenReturn(country)

          StatusFoundPageContext(null, mockResult).isZambrano shouldBe false
        }
      }

      "the product type is NOT EUS and the nationality is an EEA country" in {
        EEACountries.countries.foreach { country =>
          val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
          val fakeImmigrationStatus =
            ImmigrationStatus(LocalDate.now(), None, "WORK", "STATUS", noRecourseToPublicFunds = true)
          when(mockResult.mostRecentStatus).thenReturn(Some(fakeImmigrationStatus))
          when(mockResult.nationality).thenReturn(country)

          StatusFoundPageContext(null, mockResult).isZambrano shouldBe false
        }
      }

      "the product type is NOT EUS and the nationality is a non EEA country" in {
        nonEEACountries.foreach { country =>
          val mockResult: StatusCheckResult = mock(classOf[StatusCheckResult])
          val fakeImmigrationStatus =
            ImmigrationStatus(LocalDate.now(), None, "WORK", "STATUS", noRecourseToPublicFunds = true)
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
          val fakeImmigrationStatus =
            ImmigrationStatus(LocalDate.now(), None, "EUS", "STATUS", noRecourseToPublicFunds = true)
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
      ("EUS", "COA_IN_TIME_GRANT", "EU Settlement Scheme - Pending EU Settlement Scheme application"),
      ("EUS", "POST_GRACE_PERIOD_COA_GRANT", "EU Settlement Scheme - Pending EU Settlement Scheme application"),
      ("STUDY", "LTE", "Student - Limited leave to enter"),
      ("STUDY", "LTR", "Student - Limited leave to remain"),
      ("FRONTIER_WORKER", "PERMIT", "Frontier worker - Frontier worker permit"),
      ("SETTLEMENT", "ILR", "British National Overseas or Settlement Protection - Indefinite leave to remain")
    ).foreach { case (product, status, label) =>
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

  "RichMessages.getOrElse" should {

    "return the message if it exists" in {}

    "return the default if the message key does not exist" in {}

  }

}
