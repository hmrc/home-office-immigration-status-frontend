/*
 * Copyright 2023 HM Revenue & Customs
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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.mockito.Mockito.mock
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import repositories.SessionCacheRepository

trait ViewSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .build()

  val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  lazy val messages: Messages                      = inject[MessagesApi].preferred(Seq.empty[Lang])
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(
    doc: Document,
    cssSelector: String,
    expectedMessageKey: String,
    messages: Messages
  ): Assertion =
    assertEqualsValue(doc, cssSelector, messages(expectedMessageKey))

  def assertElementHasText(doc: Document, cssSelector: String, expectedValue: String): Assertion = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().text() == expectedValue)
  }

  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String): Assertion = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def assertRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(doc.select(cssSelector) != null, "\n\nElement " + cssSelector + " was not rendered on the page.\n")

  def assertNotRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")

  def assertCustomWidthRow(e: Elements, key: String, value: String, id: String, length: String): Assertion = {
    if (e.isEmpty) throw new IllegalArgumentException("Element not defined.")
    assert(e.hasClass("govuk-summary-list__row"))
    val keyElement = e.select("dt")
    assert(keyElement.text() == key)
    assert(keyElement.hasClass(s"govuk-summary-list__key govuk-!-width-one-$length"))
    val valueElement = e.select("dd:nth-of-type(1)")
    assert(valueElement.text() == value)
    assert(valueElement.attr("id") == id)
    assert(valueElement.hasClass(s"govuk-summary-list__value govuk-!-width-one-third"))
  }

  def assertOneThirdRowWithAction(
    e: Elements,
    key: String,
    value: String,
    id: String,
    actionText: String,
    actionUrl: String,
    rowWidth: String
  ): Assertion = {
    assertCustomWidthRow(e, key, value, id, rowWidth)
    val actionElement = e.select("dd:nth-of-type(2)")
    assert(actionElement.hasClass(s"govuk-summary-list__actions govuk-!-width-one-third"))
    assert(actionElement.select("a").text() == actionText)
    assert(actionElement.select("a").attr("href") == actionUrl)
  }
}
