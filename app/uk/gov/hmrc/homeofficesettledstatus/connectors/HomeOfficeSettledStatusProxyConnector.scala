/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.homeofficesettledstatus.connectors

import java.net.URL
import java.util.UUID

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.homeofficesettledstatus.connectors.HomeOfficeSettledStatusProxyConnector.extractResponseBody
import uk.gov.hmrc.homeofficesettledstatus.models.{StatusCheckByNinoRequest, StatusCheckResponse}
import uk.gov.hmrc.homeofficesettledstatus.wiring.AppConfig
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeSettledStatusProxyConnector @Inject()(
  appConfig: AppConfig,
  http: HttpGet with HttpPost,
  metrics: Metrics)
    extends HttpAPIMonitor {

  val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

  val baseUrl: String = appConfig.homeOfficeSettledStatusProxyBaseUrl
  val publicFundsByNinoPath = "/v1/status/public-funds/nino"

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def statusPublicFundsByNino(request: StatusCheckByNinoRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[StatusCheckResponse] =
    monitor(s"ConsumedAPI-home-office-settled-status-proxy-status-by-nino-POST") {
      http
        .POST[StatusCheckByNinoRequest, StatusCheckResponse](
          new URL(baseUrl + publicFundsByNinoPath).toExternalForm,
          request.toUpperCase)(
          implicitly[Writes[StatusCheckByNinoRequest]],
          implicitly[HttpReads[StatusCheckResponse]],
          hc.withExtraHeaders(HEADER_X_CORRELATION_ID -> UUID.randomUUID().toString),
          implicitly[ExecutionContext]
        )
        .recover {
          case e: BadRequestException =>
            Json.parse(extractResponseBody(e.message, "Response body '")).as[StatusCheckResponse]
          case e: NotFoundException =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
          case e: Upstream4xxResponse if e.upstreamResponseCode == 409 =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
        }
    }

}

object HomeOfficeSettledStatusProxyConnector {

  def extractResponseBody(message: String, prefix: String): String = {
    val pos = message.indexOf(prefix)
    val body =
      if (pos >= 0) message.substring(pos + prefix.length, message.length - 1)
      else s"""{"error":{"errCode":"$message"}}"""
    body
  }
}
