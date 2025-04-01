/*
 * Copyright 2025 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.AppConfig
import crypto.FormModelEncrypter
import models.{FormQueryModel, SearchFormModel}
import play.api.Logging
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionCacheServiceImpl @Inject() (
  sessionCacheRepository: SessionCacheRepository,
  encrypter: FormModelEncrypter,
  appConfig: AppConfig
) extends SessionCacheService
    with Logging {

  private val secretKey = appConfig.mongoEncryptionKey

  def get(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SearchFormModel]] =
    withSessionId { sessionId =>
      sessionCacheRepository
        .get(sessionId)
        .map(
          _.flatMap(formQueryModel => encrypter.decryptSearchFormModel(formQueryModel.data, sessionId, secretKey))
        )
    }

  def set(formModel: SearchFormModel)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    withSessionId { sessionId =>
      val encryptedFormModel = encrypter.encryptSearchFormModel(formModel, sessionId, secretKey)
      val formQueryModel     = FormQueryModel(sessionId, encryptedFormModel)
      sessionCacheRepository.set(formQueryModel)
    }

  def delete(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    withSessionId(sessionCacheRepository.delete(_))

  private def withSessionId[A](f: String => Future[A])(implicit hc: HeaderCarrier) =
    hc.sessionId match {
      case Some(sessionId) => f(sessionId.value)
      case None =>
        logger.error("[SessionCacheServiceImpl][withSessionId] User has no session ID", NoSessionIdException)
        Future.failed(NoSessionIdException)
    }

}

@ImplementedBy(classOf[SessionCacheServiceImpl])
trait SessionCacheService {

  def get(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SearchFormModel]]

  def set(formModel: SearchFormModel)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]

  def delete(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]

}

object NoSessionIdException extends Exception("Session id not found")
