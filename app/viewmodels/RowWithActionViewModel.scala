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

package viewmodels

import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import controllers.routes
import views.html.components.summaryRowWithAction
import models.{MrzSearchFormModel, NinoSearchFormModel, SearchFormModel}

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

  private def changeInputUrl(fieldId: String, formModel: SearchFormModel): String = formModel match {
    case _: NinoSearchFormModel => s"${routes.SearchByNinoController.onPageLoad().url}#$fieldId"
    case _: MrzSearchFormModel  => s"${routes.SearchByMrzController.onPageLoad().url}#$fieldId"
  }

  private val changeMessageKey = "generic.change"

  def apply(
    id: String,
    messageKey: String,
    data: String,
    actionId: String,
    fieldId: String,
    spanMessageKey: String,
    formModel: SearchFormModel
  ): RowWithActionViewModel =
    new RowWithActionViewModel(
      id,
      messageKey,
      data,
      changeInputUrl(fieldId, formModel),
      actionId,
      changeMessageKey,
      spanMessageKey
    )

}
