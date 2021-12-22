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
      form("foo", 2).bind(testFormFill("foo")).errors shouldBe List(FormError("foo", List("error.foo.required"), Seq()))
      /*validName(fieldName = "foo", minLenInc = 2) shouldBe Invalid("error.foo.invalid-format")
      validName(fieldName = "foo", minLenInc = 1) shouldBe Valid
      validateName("1") shouldBe invalid
      validateName("1a") shouldBe invalid
      validateName("a1") shouldBe invalid
      validateName("a1") shouldBe invalid
      validateName("Artur") shouldBe Valid
      validateName("Art ur") shouldBe Valid
      validateName("Art-ur") shouldBe Valid
      validateName("Art'ur") shouldBe Valid
      validateName("Art'ur") shouldBe Valid
      validateName("Art2ur") shouldBe invalid
      validateName("Art_ur") shouldBe invalid
      validateName("$Artur") shouldBe invalid
      validateName("@Artur") shouldBe invalid
      validateName("Ar#tur") shouldBe invalid
      validateName("ĄĘÓŚŻĆŁąęółśćńżźāēīūčģķļņšž") shouldBe Valid*/
    }
  }
}
