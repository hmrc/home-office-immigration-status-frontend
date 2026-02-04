/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.footer.FooterItem
import controllers.routes.AccessibilityStatementController

object HossFooterLinks {

  private def govukHelpLink(implicit messages: Messages): FooterItem = FooterItem(
    Some(messages("footer.links.help_page.text")),
    Some(messages("footer.links.help_page.url"))
  )

  private def accessibilityLink(implicit messages: Messages): FooterItem =
    FooterItem(
      Some(messages("footer.links.accessibility.text")),
      Some(AccessibilityStatementController.showPage.url)
    )

  def items(implicit messages: Messages): Seq[FooterItem] = Seq(
    accessibilityLink,
    govukHelpLink
  )
}
