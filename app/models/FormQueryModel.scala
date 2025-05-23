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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class FormQueryModel(
  id: String,
  data: EncryptedSearchFormModel,
  lastUpdated: Instant = Instant.now()
)

object FormQueryModel {

  implicit lazy val format: Format[FormQueryModel] = Format(reads, writes)

  implicit lazy val reads: Reads[FormQueryModel] =
    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[EncryptedSearchFormModel] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(FormQueryModel.apply)

  implicit lazy val writes: Writes[FormQueryModel] =
    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[EncryptedSearchFormModel] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(o => Tuple.fromProductTyped(o))
}
