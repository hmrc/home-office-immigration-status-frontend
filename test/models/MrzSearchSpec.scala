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

import org.mockito.Mockito.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import repositories.SessionCacheRepository

class MrzSearchSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .build()

  lazy implicit val messages: Messages = inject[MessagesApi].preferred(Seq.empty)

  "MrzSearch.documentTypeToMessageKey" should {
    "return the relevant message" when {

      Seq(
        ("PASSPORT", messages("lookup.passport")),
        ("NAT", messages("lookup.euni")),
        ("BRC", messages("lookup.res.card")),
        ("BRP", messages("lookup.res.permit")),
        ("OTHER", "OTHER")
      ).foreach { case (docType, message) =>
        s"doc type is $docType" in {
          MrzSearch.documentTypeToMessageKey(docType) mustEqual message
        }
      }
    }
  }

}
