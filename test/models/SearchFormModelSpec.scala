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

package models

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.test.Injecting

class SearchFormModelSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  lazy implicit val messages = inject[MessagesApi].preferred(Seq.empty)

  "MrzSearchFormModel.documentTypeToMessageKey" should {
    "return the relevant message" when {

      Seq(
        ("PASSPORT", messages("lookup.passport")),
        ("NAT", messages("lookup.euni")),
        ("BRC", messages("lookup.res.card")),
        ("BRP", messages("lookup.res.permit")),
        ("OTHER", "OTHER")
      ).foreach {
        case (docType, message) =>
          s"doc type is $docType" in {
            MrzSearchFormModel.documentTypeToMessageKey(docType) mustEqual message
          }
      }
    }
  }

}
