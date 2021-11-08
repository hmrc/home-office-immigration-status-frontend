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

package connectors

import java.net.URL
import java.util.UUID
import javax.inject.{Inject, Singleton}
import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import connectors.HomeOfficeImmigrationStatusProxyConnector.extractResponseBody
import models.{StatusCheckByNinoRequest, StatusCheckResponse}
import config.AppConfig
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeImmigrationStatusProxyConnector @Inject()(appConfig: AppConfig, http: HttpClient, metrics: Metrics)
    extends HttpAPIMonitor with Logging {

  private val HEADER_X_CORRELATION_ID = "X-Correlation-Id"

  private val baseUrl: String = appConfig.homeOfficeImmigrationStatusProxyBaseUrl
  private val publicFundsByNinoPath = "/v1/status/public-funds/nino"

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def statusPublicFundsByNino(
    request: StatusCheckByNinoRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[StatusCheckResponse] =
    monitor(s"ConsumedAPI-home-office-immigration-status-proxy-status-by-nino-POST") {
      http
        .POST[StatusCheckByNinoRequest, StatusCheckResponse](
          new URL(baseUrl + publicFundsByNinoPath).toExternalForm,
          request)(
          implicitly[Writes[StatusCheckByNinoRequest]],
          implicitly[HttpReads[StatusCheckResponse]],
          hc.withExtraHeaders(HEADER_X_CORRELATION_ID -> UUID.randomUUID().toString),
          implicitly[ExecutionContext]
        )
        .recover {
          case UpstreamErrorResponse.Upstream4xxResponse(e) if e.statusCode == 400 =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
          case UpstreamErrorResponse.Upstream4xxResponse(e) if e.statusCode == 404 =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
          case UpstreamErrorResponse.Upstream4xxResponse(e) if e.statusCode == 409 =>
            Json.parse(extractResponseBody(e.message, "Response body: '")).as[StatusCheckResponse]
        }
        .recoverWith {
          case e: UpstreamErrorResponse =>
            logger.error(s"${e.statusCode} error returned from the home office", e)
            Future.failed(HomeOfficeImmigrationStatusProxyError(e))
        }
    }
}

object HomeOfficeImmigrationStatusProxyConnector {

  def extractResponseBody(message: String, prefix: String): String = {
    val pos = message.indexOf(prefix)
    val body =
      if (pos >= 0) message.substring(pos + prefix.length, message.length - 1)
      else s"""{"error":{"errCode":"$message"}}"""
    body
  }
}

case class HomeOfficeImmigrationStatusProxyError(e: Throwable) extends RuntimeException(e)
