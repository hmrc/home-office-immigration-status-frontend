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

package uk.gov.hmrc.homeofficesettledstatus.journeys

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.cache.repository.CacheRepository
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto, Protected}
import uk.gov.hmrc.crypto.json.{JsonDecryptor, JsonEncryptor}
import uk.gov.hmrc.homeofficesettledstatus.repository.{SessionCache, SessionCacheRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.fsm.PersistentJourneyService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@ImplementedBy(classOf[MongoDBCachedHomeOfficeSettledStatusFrontendJourneyService])
trait HomeOfficeSettledStatusFrontendJourneyService
    extends PersistentJourneyService[HeaderCarrier] {

  val journeyKey = "HomeOfficeSettledStatusJourney"

  override val model = HomeOfficeSettledStatusFrontendJourneyModel

  // do not keep errors in the journey history
  override val breadcrumbsRetentionStrategy: Breadcrumbs => Breadcrumbs =
    _.filterNot(_.isInstanceOf[model.IsError])
}

@Singleton
class MongoDBCachedHomeOfficeSettledStatusFrontendJourneyService @Inject()(
  _cacheRepository: SessionCacheRepository,
  applicationCrypto: ApplicationCrypto)
    extends HomeOfficeSettledStatusFrontendJourneyService {

  case class PersistentState(state: model.State, breadcrumbs: List[model.State])

  implicit val crypto: CompositeSymmetricCrypto = applicationCrypto.JsonCrypto

  implicit val formats1: Format[model.State] =
    HomeOfficeSettledStatusFrontendJourneyStateFormats.formats
  implicit val formats2: Format[PersistentState] = Json.format[PersistentState]

  implicit val encryptionFormat: JsonEncryptor[PersistentState] = new JsonEncryptor()
  implicit val decryptionFormat: JsonDecryptor[PersistentState] = new JsonDecryptor()

  final val cache = new SessionCache[Protected[PersistentState]] {

    override val sessionName: String = journeyKey
    override val cacheRepository: CacheRepository = _cacheRepository

    // uses journeyId as a sessionId to persist state and breadcrumbs
    override def getSessionId(implicit hc: HeaderCarrier): Option[String] =
      hc.extraHeaders.find(_._1 == journeyKey).map(_._2)
  }

  override protected def fetch(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Option[StateAndBreadcrumbs]] =
    cache.fetch
      .map(_.map { protectedEntry =>
        val entry = protectedEntry.decryptedValue
        (entry.state, entry.breadcrumbs)
      })
      .transform {
        case Success(value) => Success(value)
        case Failure(error) =>
          error.printStackTrace()
          Failure(error)
      }

  override protected def save(state: StateAndBreadcrumbs)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[StateAndBreadcrumbs] = {
    val entry = PersistentState(state._1, state._2)
    val protectedEntry = Protected(entry)
    cache
      .save(protectedEntry)
      .map(_ => state)
      .transform {
        case Success(value) => Success(value)
        case Failure(error) =>
          error.printStackTrace()
          Failure(error)
      }
  }

  override def clear(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    cache.delete().map(_ => ())

}
