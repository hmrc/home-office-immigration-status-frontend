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

package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import repositories.SessionCacheRepository
import models.{FormQueryModel, StatusCheckByNinoFormModel}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDateTime
import play.api.libs.json.Json

@Singleton
class SessionCacheServiceImpl @Inject()(
  sessionCacheRepository: SessionCacheRepository
) extends SessionCacheService {

  def get(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[FormQueryModel]] =
    withSessionId(sessionCacheRepository.findById(_))

  def set(formModel: StatusCheckByNinoFormModel, now: LocalDateTime = LocalDateTime.now)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Unit] =
    withSessionId { sessionId =>
      val formQueryModel = FormQueryModel(sessionId, formModel)
      val selector = Json.obj("_id"  -> formQueryModel.id)
      val modifier = Json.obj("$set" -> (formQueryModel copy (lastUpdated = now)))
      sessionCacheRepository.findAndUpdate(query = selector, update = modifier, upsert = true).map(_ => ())
    }

  def delete(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    withSessionId(sessionCacheRepository.removeById(_).map(_ => ()))

  private def withSessionId[A](f: String => Future[A])(implicit hc: HeaderCarrier) =
    hc.sessionId match {
      case Some(sessionId) => f(sessionId.value)
      case None            => Future.failed(NoSessionIdException)
    }

}

@ImplementedBy(classOf[SessionCacheServiceImpl])
trait SessionCacheService {

  def get(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[FormQueryModel]]

  def set(formModel: StatusCheckByNinoFormModel, now: LocalDateTime = LocalDateTime.now)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Unit]

  def delete(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]

}

object NoSessionIdException extends Exception("Session id not found")