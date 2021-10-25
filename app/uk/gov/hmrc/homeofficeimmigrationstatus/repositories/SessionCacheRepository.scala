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

package uk.gov.hmrc.homeofficeimmigrationstatus.repositories

import com.google.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.homeofficeimmigrationstatus.config.AppConfig
import uk.gov.hmrc.homeofficeimmigrationstatus.models.FormQueryModel
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

@Singleton
class SessionCacheRepository @Inject()(
  mongoComponent: ReactiveMongoComponent,
  appConfig: AppConfig
) extends ReactiveRepository[FormQueryModel, String](
      collectionName = appConfig.mongoCollectionName,
      mongo = mongoComponent.mongoConnector.db,
      domainFormat = FormQueryModel.formats,
      idFormat = implicitly
    ) {

  private val TTL = Index(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = Some("lastUpdatedTTL"),
    options = BSONDocument("expireAfterSeconds" -> appConfig.mongoSessionExpiration)
  )

  override def indexes: Seq[Index] = Seq(TTL)
}
