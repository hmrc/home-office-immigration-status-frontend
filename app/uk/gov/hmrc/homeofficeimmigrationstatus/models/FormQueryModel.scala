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

package uk.gov.hmrc.homeofficeimmigrationstatus.models

import play.api.libs.json.{Format, JsError, JsSuccess, JsValue, Json, Reads, Writes, __}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.mongoEntity

import java.time.LocalDateTime
import scala.util.{Failure, Success}

case class FormQueryModel(
  id: BSONObjectID,
  data: StatusCheckByNinoFormModel,
  lastUpdated: LocalDateTime = LocalDateTime.now()) {
  id.stringify
}

object FormQueryModel {
  implicit val formats: Format[FormQueryModel] = mongoEntity { Json.format[FormQueryModel] }

  implicit val objectIdRead: Reads[String] = Reads[String] { json =>
    (json \ "$id" \"$oid").validate[String].flatMap { str =>
      JsSuccess(str)
    }
  }

  implicit val objectIdWrite: Writes[String] = new Writes[String] {
    def writes(objectId: String): JsValue = Json.obj(
      "$oid" -> objectId
    )
  }

  implicit val idFormats: Format[String] = Format(objectIdRead, objectIdWrite)
}
