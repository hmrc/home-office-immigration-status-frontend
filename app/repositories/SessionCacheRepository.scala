/*
 * Copyright 2026 HM Revenue & Customs
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

package repositories

import java.util.concurrent.TimeUnit
import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.AppConfig
import models.FormQueryModel
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{FindOneAndReplaceOptions, IndexModel, IndexOptions}
import org.mongodb.scala.model.Indexes.ascending
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import repositories.SessionCacheRepositoryImpl.CollectionName
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[SessionCacheRepositoryImpl])
trait SessionCacheRepository {
  def get(id: String)(implicit ec: ExecutionContext): Future[Option[FormQueryModel]] 

  def set(formQueryModel: FormQueryModel)(implicit ec: ExecutionContext): Future[Unit]

  def delete(id: String)(implicit ec: ExecutionContext): Future[Unit]
}

@Singleton
class SessionCacheRepositoryImpl @Inject()(
  mongoComponent: MongoComponent,
  appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[FormQueryModel](
      collectionName = CollectionName,
      mongoComponent = mongoComponent,
      domainFormat = FormQueryModel.formQueryModelFormat,
      indexes = Seq(
        IndexModel(
          ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedTTL")
            .expireAfter(appConfig.mongoSessionExpiration.toLong, TimeUnit.SECONDS)
        )
      )
    ) with SessionCacheRepository {

  override def get(id: String)(implicit ec: ExecutionContext): Future[Option[FormQueryModel]] =
    collection.find(equal("_id", id)).toFuture().map(_.headOption)

  override def set(formQueryModel: FormQueryModel)(implicit ec: ExecutionContext): Future[Unit] = {
    val query = equal("_id", formQueryModel.id)
    collection
      .findOneAndReplace(query, formQueryModel, FindOneAndReplaceOptions().upsert(true))
      .toFuture()
      .map(_ => ())
  }

  override def delete(id: String)(implicit ec: ExecutionContext): Future[Unit] =
    collection.deleteOne(equal("_id", id)).toFuture().map(_ => ())    
    }


object SessionCacheRepositoryImpl {
  val CollectionName = "form-query"
}

