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

package views.components

import play.api.data.Form
import play.api.i18n.Messages

object InputDateHelper {

  def concatDateErrors(id: String, form: Form[_], appended: Seq[String])(
    implicit messages: Messages): Option[String] = {
    val allFieldErrors: Seq[String] = appended.flatMap(suffix => form(s"$id$suffix").error).flatMap(_.messages)
    allFieldErrors match {
      case Nil         => None
      case err :: errs => Some((messages(err) +: errs.map(e => lowercaseFirstChar(messages(e)))).mkString(", "))
    }
  }

  def lowercaseFirstChar(str: String): String =
    str.toList match {
      case first :: rest => (first.toLower +: rest).mkString("")
      case _             => str
    }

}
