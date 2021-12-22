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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, single, tuple}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.test.Injecting

class InputDateHelperSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  lazy val messages: Messages = inject[MessagesApi].preferred(Seq.empty[Lang])

  def fieldsMapping = tuple("field1" -> nonEmptyText, "field2" -> nonEmptyText, "field3" -> nonEmptyText)
  val form = Form(single("myField" -> fieldsMapping))
  def testFormFill(field1: String, field2: String, field3: String): Map[String, String] =
    Map("myField.field1" -> field1, "myField.field2" -> field2, "myField.field3" -> field3)

  "concatDateErrors" should {
    "return None for an empty list of errors" in {
      val filledForm = form.bind(testFormFill("abc", "abc", "abc"))
      InputDateHelper.concatDateErrors("myField", filledForm, Seq("field1", "field2", "field3"))(messages) mustBe None
    }

    "return the single error message where one error occurred" in {
      val filledForm = form.bind(testFormFill("", "abc", "abc"))
      InputDateHelper.concatDateErrors("myField", filledForm, Seq(".field1", ".field2", ".field3"))(messages) mustBe Some(
        "This field is required")
    }

    "return two errors separated by a comma where two errors occurred" in {
      val filledForm = form.bind(testFormFill("", "", "abc"))
      InputDateHelper.concatDateErrors("myField", filledForm, Seq(".field1", ".field2", ".field3"))(messages) mustBe Some(
        "This field is required, this field is required")
    }

    "return three errors separated by commas where three errors occurred" in {
      val filledForm = form.bind(testFormFill("", "", ""))
      InputDateHelper.concatDateErrors("myField", filledForm, Seq(".field1", ".field2", ".field3"))(messages) mustBe Some(
        "This field is required, this field is required, this field is required")
    }

    "return None where the errors don't relate to the suffixes" in {
      val filledForm = form.bind(testFormFill("", "", ""))
      InputDateHelper.concatDateErrors("myField", filledForm, Seq(".field4"))(messages) mustBe None
    }

    "return the single error message where one error occurred for the given suffix" in {
      val filledForm = form.bind(testFormFill("", "", ""))
      InputDateHelper.concatDateErrors("myField", filledForm, Seq(".field1"))(messages) mustBe Some(
        "This field is required")
    }
  }

}
