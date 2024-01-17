/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats

sealed trait EncryptedSearchFormModel

object EncryptedSearchFormModel {
  implicit val formats: OFormat[EncryptedSearchFormModel] = Json.format[EncryptedSearchFormModel]
}

final case class EncryptedNinoSearchFormModel(
  nino: EncryptedValue,
  givenName: EncryptedValue,
  familyName: EncryptedValue,
  dateOfBirth: EncryptedValue
) extends EncryptedSearchFormModel

object EncryptedNinoSearchFormModel {
  implicit val encryptedValueFormat: Format[EncryptedValue]   = CryptoFormats.encryptedValueFormat
  implicit val formats: OFormat[EncryptedNinoSearchFormModel] = Json.format[EncryptedNinoSearchFormModel]
}

final case class EncryptedMrzSearchFormModel(
  documentType: EncryptedValue,
  documentNumber: EncryptedValue,
  dateOfBirth: EncryptedValue,
  nationality: EncryptedValue
) extends EncryptedSearchFormModel

object EncryptedMrzSearchFormModel {
  implicit val encryptedValueFormat: Format[EncryptedValue]  = CryptoFormats.encryptedValueFormat
  implicit val formats: OFormat[EncryptedMrzSearchFormModel] = Json.format[EncryptedMrzSearchFormModel]
}
