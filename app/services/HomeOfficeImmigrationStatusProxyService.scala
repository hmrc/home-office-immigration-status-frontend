/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import java.util.UUID

import javax.inject.{Inject, Singleton}
import models.{MrzSearch, NinoSearch, Search, SearchFormModel, StatusCheckResponseWithStatus}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import connectors.{Constants, HomeOfficeImmigrationStatusProxyConnector}
import config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeImmigrationStatusProxyService @Inject() (
  connector: HomeOfficeImmigrationStatusProxyConnector,
  auditService: AuditService
) {

  def search(query: SearchFormModel)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext,
    request: Request[Any],
    appConfig: AppConfig
  ): Future[StatusCheckResponseWithStatus] = {

    val correlationId: String = UUID.randomUUID().toString
    val headerCarrier         = hc.withExtraHeaders(Constants.HEADER_X_CORRELATION_ID -> correlationId)
    val searchFromRequest     = query.toSearch(appConfig.defaultQueryTimeRangeInMonths)

    sendRequestAuditingResults(searchFromRequest) {
      searchFromRequest match {
        case search: NinoSearch => connector.statusPublicFundsByNino(search)(headerCarrier, ec)
        case search: MrzSearch  => connector.statusPublicFundsByMrz(search)(headerCarrier, ec)
      }
    }
  }

  private def sendRequestAuditingResults[A](search: Search)(
    future: Future[StatusCheckResponseWithStatus]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[Any]): Future[StatusCheckResponseWithStatus] =
    future.map { result =>
      auditService.auditStatusCheckEvent(search, result)
      result
    }

}
