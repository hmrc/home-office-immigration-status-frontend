/*
 * Copyright 2024 HM Revenue & Customs
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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status.NO_CONTENT
import support.WireMockSupport

trait DataStreamStubs extends Eventually {
  me: WireMockSupport =>

  private val (timeout: Int, interval: Int) = (5, 500)

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(timeout, Seconds)), scaled(Span(interval, Millis)))

  def givenAuditConnector(): Unit = {
    stubFor(post(urlPathEqualTo(auditUrl)).willReturn(aResponse().withStatus(NO_CONTENT)))
    stubFor(post(urlPathEqualTo(auditUrl + "/merged")).willReturn(aResponse().withStatus(NO_CONTENT)))
  }

  private def auditUrl = "/write/audit"
}
