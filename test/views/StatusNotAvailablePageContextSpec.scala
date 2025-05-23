/*
 * Copyright 2025 HM Revenue & Customs
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
import models.{MrzSearchFormModel, NinoSearchFormModel, StatusCheckResult}
import org.mockito.ArgumentMatchers.{any, matches}
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, mock, reset, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import repositories.SessionCacheRepository
import utils.NinoGenerator
import viewmodels.RowViewModel

import java.time.LocalDate
import java.util.Locale

class StatusNotAvailablePageContextSpec
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

  lazy val realMessages: Messages = inject[MessagesApi].preferred(Seq.empty[Lang])
  val mockMessages: Messages      = mock(classOf[MessagesImpl], RETURNS_DEEP_STUBS)
  val currentStatusLabelMsg       = "current status label msg"

  lazy val countries: Countries = inject[Countries]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessages)
    when(mockMessages(matches("status-not-available\\.current.*"), any())).thenReturn(currentStatusLabelMsg)
  }

  "notAvailablePersonalData" should {
    "populate the row objects correctly for a nino search" when {
      val dob    = LocalDate.now()
      val query  = NinoSearchFormModel(NinoGenerator.generateNino, "Surname", "Forename", dob)
      val result = StatusCheckResult("Full name", dob, "JPN", Nil)

      def createContext: StatusNotAvailablePageContext = StatusNotAvailablePageContext(query, result)

      Seq(
        ("nino", "generic.nino", query.nino.nino),
        ("nationality", "generic.nationality", countries.getCountryNameFor(result.nationality)),
        ("dob", "generic.dob", DateFormat.format(Locale.UK)(query.dateOfBirth))
      ).foreach { case (id, msgKey, data) =>
        s"row is for $id" in {
          assert(
            createContext.notAvailablePersonalData(countries)(realMessages).contains(RowViewModel(id, msgKey, data))
          )
        }
      }
    }

    "populate the row objects correctly for an mrz search" when {
      val dob    = LocalDate.now()
      val query  = MrzSearchFormModel("PASSPORT", "12345", dob, "JPN")
      val result = StatusCheckResult("Full name", dob, "JPN", Nil)

      def createContext: StatusNotAvailablePageContext = StatusNotAvailablePageContext(query, result)

      Seq(
        ("documentType", "lookup.identity.label", "Passport"),
        ("documentNumber", "lookup.mrz.label", query.documentNumber),
        ("nationality", "generic.nationality", countries.getCountryNameFor(result.nationality)),
        ("dob", "generic.dob", DateFormat.format(Locale.UK)(query.dateOfBirth))
      ).foreach { case (id, msgKey, data) =>
        s"row is for $id" in {
          assert(
            createContext.notAvailablePersonalData(countries)(realMessages).contains(RowViewModel(id, msgKey, data))
          )
        }
      }
    }
  }

}
