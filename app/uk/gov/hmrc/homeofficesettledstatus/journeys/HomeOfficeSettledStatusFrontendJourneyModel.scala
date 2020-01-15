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

package uk.gov.hmrc.homeofficesettledstatus.journeys

import uk.gov.hmrc.homeofficesettledstatus.models.HomeOfficeSettledStatusFrontendModel
import uk.gov.hmrc.play.fsm.JourneyModel

object HomeOfficeSettledStatusFrontendJourneyModel extends JourneyModel {

  sealed trait State
  sealed trait IsError

  override val root: State = State.Start

  object State {
    case object Start extends State
    case class End(
      name: String,
      postcode: Option[String],
      telephone: Option[String],
      emailAddress: Option[String])
        extends State
    case object SomeError extends State with IsError
  }

  object Transitions {
    import State._

    def start = Transition {
      case _ => goto(Start)
    }

    def submitStart(humanId: String)(formData: HomeOfficeSettledStatusFrontendModel) = Transition {
      case Start =>
        goto(End(formData.name, formData.postcode, formData.telephoneNumber, formData.emailAddress))
    }
  }

}
