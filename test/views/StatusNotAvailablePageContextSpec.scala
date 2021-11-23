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

package views

import models.NinoSearchFormModel
import org.mockito.ArgumentMatchers.{any, matches}
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, mock, reset, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.Call
import uk.gov.hmrc.domain.Nino
import utils.NinoGenerator
import viewmodels.RowViewModel

import java.time.LocalDate
import java.util.Locale

class StatusNotAvailablePageContextSpec
    extends AnyWordSpecLike with Matchers with OptionValues with BeforeAndAfterEach with GuiceOneAppPerSuite {

  val realMessages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty[Lang])
  val mockMessages: Messages = mock(classOf[MessagesImpl], RETURNS_DEEP_STUBS)
  val currentStatusLabelMsg = "current status label msg"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMessages)
    when(mockMessages(matches("status-not-available\\.current.*"), any())).thenReturn(currentStatusLabelMsg)
  }

  val query = NinoSearchFormModel(NinoGenerator.generateNino, "Surname", "Forename", LocalDate.now())
  val call = Call("GET", "/")

  def createContext = StatusNotAvailablePageContext(query, call)

  "notAvailablePersonalData" must {
    "populate the row objects correctly" when {
      Seq(
        ("nino", "generic.nino", query.nino.nino),
        ("givenName", "generic.givenName", query.givenName),
        ("familyName", "generic.familyName", query.familyName),
        ("dob", "generic.dob", DateFormat.format(Locale.UK)(query.dateOfBirth))
      ).foreach {
        case (id, msgKey, data) =>
          s"row is for $id" in {
            assert(createContext.notAvailablePersonalData(realMessages).contains(RowViewModel(id, msgKey, data)))
          }
      }
    }
  }
}
