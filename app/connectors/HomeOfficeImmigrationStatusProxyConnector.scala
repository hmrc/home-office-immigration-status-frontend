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

package connectors

import config.AppConfig
import connectors.StatusCheckResponseHttpParser._
import models.{MrzSearch, NinoSearch, StatusCheckResponseWithStatus}
import uk.gov.hmrc.http._

import java.net.URL
import java.util.UUID.randomUUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeImmigrationStatusProxyConnector @Inject() (appConfig: AppConfig, http: HttpClient) {

  private val baseUrl: String       = appConfig.homeOfficeImmigrationStatusProxyBaseUrl
  private val publicFundsByNinoPath = "/v1/status/public-funds/nino"
  private val publicFundsByMrzPath  = "/v1/status/public-funds/mrz"

  private[connectors] def generateNewUUID: String = randomUUID.toString

  private[connectors] def correlationId(hc: HeaderCarrier): String = {
    val uuidLength           = 24
    val CorrelationIdPattern = """.*([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}).*""".r
    hc.requestId match {
      case Some(requestId) =>
        requestId.value match {
          case CorrelationIdPattern(prefix) => prefix + "-" + generateNewUUID.substring(uuidLength)
          case _                            => generateNewUUID
        }
      case _ => generateNewUUID
    }
  }

  def statusPublicFundsByNino(
    request: NinoSearch
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[StatusCheckResponseWithStatus] = {
    val hc: HeaderCarrier = addExtraHeaders(headerCarrier)
    doPostByNino(request)(hc, ec)
  }

  private def doPostByNino(
    request: NinoSearch
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[StatusCheckResponseWithStatus] =
    http
      .POST[NinoSearch, StatusCheckResponseWithStatus](new URL(baseUrl + publicFundsByNinoPath).toExternalForm, request)

  def statusPublicFundsByMrz(
    request: MrzSearch
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[StatusCheckResponseWithStatus] = {
    val hc: HeaderCarrier = addExtraHeaders(headerCarrier)
    doPostByMrz(request)(hc, ec)
  }

  private def doPostByMrz(
    request: MrzSearch
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[StatusCheckResponseWithStatus] =
    http.POST[MrzSearch, StatusCheckResponseWithStatus](new URL(baseUrl + publicFundsByMrzPath).toExternalForm, request)

  private def addExtraHeaders(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("CorrelationId" -> correlationId(headerCarrier))
}
