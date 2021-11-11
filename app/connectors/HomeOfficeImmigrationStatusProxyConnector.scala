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
import models.{HomeOfficeError, StatusCheckByNinoRequest, StatusCheckResponse}
import config.AppConfig
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.Logging
import connectors.StatusCheckResponseHttpParser._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeImmigrationStatusProxyConnector @Inject()(appConfig: AppConfig, http: HttpClient, metrics: Metrics)
    extends HttpAPIMonitor with Logging {

  private val baseUrl: String = appConfig.homeOfficeImmigrationStatusProxyBaseUrl
  private val publicFundsByNinoPath = "/v1/status/public-funds/nino"

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def statusPublicFundsByNino(request: StatusCheckByNinoRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Either[HomeOfficeError, StatusCheckResponse]] =
    monitor("ConsumedAPI-home-office-immigration-status-proxy-status-by-nino-POST") {
      http
        .POST[StatusCheckByNinoRequest, Either[HomeOfficeError, StatusCheckResponse]](
          new URL(baseUrl + publicFundsByNinoPath).toExternalForm,
          request)
    }

}
