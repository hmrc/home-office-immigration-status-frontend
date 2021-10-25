package uk.gov.hmrc.homeofficeimmigrationstatus.repositories

import com.google.inject.{ImplementedBy, Inject}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.homeofficeimmigrationstatus.config.AppConfig
import uk.gov.hmrc.homeofficeimmigrationstatus.models.StatusCheckByNinoFormModel
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

@Singleton
class SessionCacheRepositoryImpl @Inject()(
                                            mongoComponent: ReactiveMongoComponent,
                                            appConfig: AppConfig
                                          )
  extends ReactiveRepository[StatusCheckByNinoFormModel, BSONObjectID](
  collectionName = appConfig.mongoCollectionName,
  mongo          = mongoComponent.mongoConnector.db,
  domainFormat   = StatusCheckByNinoFormModel.formats,
  idFormat       = ReactiveMongoFormats.objectIdFormats
  ){

  override def indexes: Seq[Index] = Seq(
    Index(Seq("lastUpdated" -> IndexType.Ascending), name = Some("lastUpdatedTTL"), unique = false, sparse = true)
  )
}

@ImplementedBy(classOf[SessionCacheRepositoryImpl])
trait SessionCacheRepository
