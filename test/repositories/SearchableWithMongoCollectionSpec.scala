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

package repositories

import java.time.{LocalDate, LocalDateTime}

import crypto.{FormModelEncrypter, TestGCMCipher}
import models.{FormQueryModel, NinoSearchFormModel}
import org.mockito.ArgumentMatchers.{any, eq, refEq}
import org.mongodb.scala.{FindObservable, MongoCollection, SingleObservable}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import org.mockito.Mockito.{mock, never, reset, verify, when}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{Filters, FindOneAndReplaceOptions}
import org.mongodb.scala.result.DeleteResult
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import utils.NinoGenerator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SearchableWithMongoCollectionSpec
    extends PlaySpec with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
    )
    .build()

  val mockSessionCacheRepository: SessionCacheRepository = mock(classOf[SessionCacheRepository])

  val mockFindObs = mock(classOf[FindObservable[FormQueryModel]])
  val mockSingleObs = mock(classOf[SingleObservable[Seq[FormQueryModel]]])
  val mockSingleObsDelete = mock(classOf[SingleObservable[DeleteResult]])
  val mockDeleteResult = mock(classOf[DeleteResult])
  val mockCollection: MongoCollection[FormQueryModel] = mock(classOf[MongoCollection[FormQueryModel]])

  override def beforeEach(): Unit = {
    reset(mockFindObs)
    reset(mockSingleObs)
    reset(mockCollection)
    reset(mockSingleObsDelete)
    reset(mockDeleteResult)
    when(mockCollection.find[FormQueryModel](any(classOf[Bson]))(any(), any())).thenReturn(mockFindObs)
    when(mockFindObs.collect).thenReturn(mockSingleObs)
    super.beforeEach
  }

  val now = LocalDateTime.now
  private val cipher = new TestGCMCipher
  private val encrypter = new FormModelEncrypter(cipher)
  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  val formModel = NinoSearchFormModel(
    nino = NinoGenerator.generateNino,
    givenName = "Jimmy",
    familyName = "Jazz",
    dateOfBirth = LocalDate.now)
  val encryptedFormModel = encrypter.encryptSearchFormModel(formModel, "123", secretKey)
  val formQuery = FormQueryModel(id = "ID1", data = encryptedFormModel, now)

  val filters: Bson = Filters.equal("_id", "ID1")

  val unit = ()

  object TestSearchable extends SearchableWithMongoCollection {
    val collection: MongoCollection[FormQueryModel] = mockCollection
  }

  "get" must {
    "call collection.find and find a result" in {
      when(mockSingleObs.head).thenReturn(Future.successful(Seq(formQuery)))

      TestSearchable.get("ID1").map { result =>
        verify(mockCollection).find(refEq(filters))(any(), any())
        result mustEqual Some(formQuery)
      }
    }

    "call collection.find and not find a result" in {
      when(mockSingleObs.head).thenReturn(Future.successful(Nil))

      TestSearchable.get("ID1").map { result =>
        verify(mockCollection).find(refEq(filters))(any(), any())
        result mustEqual None
      }
    }
  }

  "set" must {
    "call collection.findOneAndReplace" in {
      when(mockCollection.findOneAndReplace(any(), any(), any(classOf[FindOneAndReplaceOptions])))
        .thenReturn(mockFindObs)
      when(mockSingleObs.head).thenReturn(Future.successful(Seq(formQuery)))

      val filters: Bson = Filters.equal("_id", "ID1")

      TestSearchable.set(formQuery).map { result =>
        verify(mockCollection)
          .findOneAndReplace(refEq(filters), refEq(formQuery), refEq(FindOneAndReplaceOptions().upsert(true)))
        result mustEqual unit
      }
    }
  }

  "delete" must {
    "call collection.deleteOne" in {
      when(mockCollection.deleteOne(any())).thenReturn(mockSingleObsDelete)
      when(mockSingleObsDelete.head).thenReturn(Future.successful(mockDeleteResult))

      TestSearchable.delete("ID1").map { result =>
        verify(mockCollection).deleteOne(refEq(filters))
        result mustEqual unit
      }
    }

  }
}
