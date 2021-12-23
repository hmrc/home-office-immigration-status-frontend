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

package forms

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.Forms.single
import play.api.data.{Form, FormError, Mapping}
import play.api.data.validation.{Invalid, Valid}

class FormFieldMappingsSpec extends AnyWordSpecLike with Matchers with OptionValues with FormFieldMappings {

  def validateName(errorName: String, len: Int): Mapping[String] = validName(fieldName = errorName, minLenInc = len)
  val invalid: Invalid = Invalid("error.bar.invalid-format")

  def form(name: String, min: Int) = Form(single(name -> validateName(name, min)))
  def testFormFill(map: String) = Map("foo" -> map)

  "FormFieldMappings" should {
    "checks emptiness" in {
      form("foo", 2).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))
    }

    "validate name" in {
      form("foo", 2).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))
      form("foo", 1).bind(testFormFill("")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))

      form("1", 2).bind(testFormFill("")).errors shouldBe List(FormError("1", List("error.1.required"), Seq()))
      form("1", 1).bind(testFormFill("")).errors shouldBe List(FormError("1", List("error.1.required"), Seq()))
      form("a1", 2).bind(testFormFill("")).errors shouldBe List(FormError("a1", List("error.a1.required"), Seq()))
      form("a1", 1).bind(testFormFill("")).errors shouldBe List(FormError("a1", List("error.a1.required"), Seq()))
      form("1a", 2).bind(testFormFill("")).errors shouldBe List(FormError("1a", List("error.1a.required"), Seq()))
      form("1a", 1).bind(testFormFill("")).errors shouldBe List(FormError("1a", List("error.1a.required"), Seq()))

      form("Artur", 2).bind(testFormFill("")).errors shouldBe List(
        FormError("Artur", List("error.Artur.required"), Seq()))
      form("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž", 2).bind(testFormFill("")).errors shouldBe List(
        FormError("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž", List("error.ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž.required"), Seq()))

    }
  }
}
