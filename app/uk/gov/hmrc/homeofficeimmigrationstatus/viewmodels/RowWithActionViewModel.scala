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

package uk.gov.hmrc.homeofficeimmigrationstatus.viewmodels

import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.homeofficeimmigrationstatus.controllers.routes
import uk.gov.hmrc.homeofficeimmigrationstatus.views.html.components.summaryRowWithAction

final case class RowWithActionViewModel(
  id: String,
  messageKey: String,
  data: String,
  actionUrl: String,
  actionId: String,
  actionMessageKey: String,
  spanMessageKey: String
) extends RowHtml {
  override def html(implicit messages: Messages): HtmlFormat.Appendable = summaryRowWithAction(this)
}

object RowWithActionViewModel {
  private def changeInputUrl(fieldId: String) = s"${routes.StatusCheckByNinoController.onPageLoad.url}#$fieldId"
  private val changeMessageKey = "generic.change"

  def apply(
    id: String,
    messageKey: String,
    data: String,
    actionId: String,
    fieldId: String,
    spanMessageKey: String): RowWithActionViewModel =
    new RowWithActionViewModel(
      id,
      messageKey,
      data,
      changeInputUrl(fieldId),
      actionId,
      changeMessageKey,
      spanMessageKey)

  def apply(
    id: String,
    messageKey: String,
    data: String,
    actionId: String,
    fieldId: String
  ): RowWithActionViewModel =
    new RowWithActionViewModel(id, messageKey, data, changeInputUrl(fieldId), actionId, changeMessageKey, messageKey)
}
