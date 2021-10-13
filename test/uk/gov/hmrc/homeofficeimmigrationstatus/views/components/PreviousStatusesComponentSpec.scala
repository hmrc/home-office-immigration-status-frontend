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

package uk.gov.hmrc.homeofficeimmigrationstatus.views.components

import org.jsoup.nodes.Document
import uk.gov.hmrc.homeofficeimmigrationstatus.views.ViewSpec
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.components.PreviousStatuses

class PreviousStatusesComponentSpec extends ViewSpec {

  val sut: PreviousStatuses = inject[PreviousStatuses]

  //this page is subject to be changed a lot in future tickets, not testing the old code just to delete later.
  //todo the rest of this spec

  "PreviousStatusesComponent" must {
    "show nothing" when {
      "no previous statuses" in {
        val doc: Document = asDocument(sut(Nil)(messages))
        val emptyDocument = Document.createShell("")

        doc.toString mustBe emptyDocument.toString
      }
    }
  }
}
