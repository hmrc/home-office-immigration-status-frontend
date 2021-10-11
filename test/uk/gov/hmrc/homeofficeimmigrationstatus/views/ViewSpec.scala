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

package uk.gov.hmrc.homeofficeimmigrationstatus.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html

trait ViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  val messages: Messages = inject[MessagesApi].preferred(Seq.empty[Lang])
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String, messages: Messages) =
    assertEqualsValue(doc, cssSelector, messages(expectedMessageKey))

  def assertElementHasText(doc: Document, cssSelector: String, expectedValue: String) = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().text() == expectedValue)
  }

  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String) = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertRenderedById(doc: Document, id: String) =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def assertRenderedByCssSelector(doc: Document, cssSelector: String) =
    assert(doc.select(cssSelector) != null, "\n\nElement " + cssSelector + " was not rendered on the page.\n")

  def assertNotRenderedById(doc: Document, id: String) =
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")

  def assertOneThirdRow(e: Elements, key: String, value: String, id: String): Assertion = {
    if (e.isEmpty) throw new IllegalArgumentException("Element not defined.")
    assert(e.hasClass("govuk-summary-list__row"))
    val keyElement = e.select("dt")
    assert(keyElement.text() == key)
    assert(keyElement.hasClass("govuk-summary-list__key govuk-!-width-one-third"))
    val valueElement = e.select("dd")
    assert(valueElement.text() == value)
    assert(valueElement.attr("id") == id)
    assert(valueElement.hasClass("govuk-summary-list__value govuk-!-width-one-third"))
  }
}
