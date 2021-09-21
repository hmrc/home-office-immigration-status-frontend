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

package uk.gov.hmrc.homeofficesettledstatus.services

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Format
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.homeofficesettledstatus.journeys.{HomeOfficeSettledStatusFrontendJourneyModel, HomeOfficeSettledStatusFrontendJourneyStateFormats}
import uk.gov.hmrc.homeofficesettledstatus.repository.CacheRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.fsm.PersistentJourneyService

trait HomeOfficeSettledStatusFrontendJourneyService[RequestContext] extends PersistentJourneyService[RequestContext] {

  val journeyKey: String = "HomeOfficeSettledStatusJourney"

  override val model: HomeOfficeSettledStatusFrontendJourneyModel.type = HomeOfficeSettledStatusFrontendJourneyModel

  // do not keep errors in the journey history
  override val breadcrumbsRetentionStrategy: Breadcrumbs => Breadcrumbs =
    _.filterNot(_.isInstanceOf[model.IsError])
}

trait HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier
    extends HomeOfficeSettledStatusFrontendJourneyService[HeaderCarrier]

@Singleton
case class MongoDBCachedHomeOfficeSettledStatusFrontendJourneyService @Inject()(
  cacheRepository: CacheRepository,
  applicationCrypto: ApplicationCrypto)
    extends MongoDBCachedJourneyService[HeaderCarrier]
    with HomeOfficeSettledStatusFrontendJourneyServiceWithHeaderCarrier {

  override val stateFormats: Format[model.State] =
    HomeOfficeSettledStatusFrontendJourneyStateFormats.formats

  override def getJourneyId(hc: HeaderCarrier): Option[String] =
    hc.extraHeaders.find(_._1 == journeyKey).map(_._2)

}
