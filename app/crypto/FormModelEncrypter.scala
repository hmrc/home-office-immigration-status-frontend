/*
 * Copyright 2023 HM Revenue & Customs
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

package crypto

import com.google.inject.Singleton
import models._
import uk.gov.hmrc.crypto.{EncryptedValue, SymmetricCryptoFactory}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import scala.util.Try

@Singleton
class FormModelEncrypter {

  def encryptSearchFormModel(formModel: SearchFormModel, sessionId: String, key: String): EncryptedSearchFormModel =
    formModel match {
      case model: NinoSearchFormModel => encryptNinoSearchFormModel(model, key)(sessionId)
      case model: MrzSearchFormModel  => encryptMrzSearchFormModel(model, key)(sessionId)
    }

  def decryptSearchFormModel(
    formModel: EncryptedSearchFormModel,
    sessionId: String,
    key: String
  ): Option[SearchFormModel] =
    formModel match {
      case model: EncryptedNinoSearchFormModel => decryptNinoSearchFormModel(model, key)(sessionId)
      case model: EncryptedMrzSearchFormModel  => decryptMrzSearchFormModel(model, key)(sessionId)
    }

  private def encryptNinoSearchFormModel(
    formModel: NinoSearchFormModel,
    key: String
  )(implicit sessionId: String): EncryptedNinoSearchFormModel =
    EncryptedNinoSearchFormModel(
      encrypt(formModel.nino.toString, key),
      encrypt(formModel.givenName, key),
      encrypt(formModel.familyName, key),
      encrypt(formModel.dateOfBirth.toString, key)
    )

  private def decryptNinoSearchFormModel(
    formModel: EncryptedNinoSearchFormModel,
    key: String
  )(implicit sessionId: String): Option[NinoSearchFormModel] =
    (for {
      nino <- Try(Nino(decrypt(formModel.nino, key)))
      dob  <- Try(LocalDate.parse(decrypt(formModel.dateOfBirth, key)))
      formModel <-
        Try(NinoSearchFormModel(nino, decrypt(formModel.givenName, key), decrypt(formModel.familyName, key), dob))
    } yield formModel).toOption

  private def encryptMrzSearchFormModel(
    formModel: MrzSearchFormModel,
    key: String
  )(implicit sessionId: String): EncryptedMrzSearchFormModel =
    EncryptedMrzSearchFormModel(
      encrypt(formModel.documentType, key),
      encrypt(formModel.documentNumber, key),
      encrypt(formModel.dateOfBirth.toString, key),
      encrypt(formModel.nationality, key)
    )

  private def decryptMrzSearchFormModel(
    formModel: EncryptedMrzSearchFormModel,
    key: String
  )(implicit sessionId: String): Option[MrzSearchFormModel] =
    (for {
      dob <- Try(LocalDate.parse(decrypt(formModel.dateOfBirth, key)))
      formModel <-
        Try(
          MrzSearchFormModel(
            decrypt(formModel.documentType, key),
            decrypt(formModel.documentNumber, key),
            dob,
            decrypt(formModel.nationality, key)
          )
        )
    } yield formModel).toOption

  private def encrypt(field: String, key: String)(implicit sessionId: String): EncryptedValue =
    SymmetricCryptoFactory.aesGcmAdCrypto(key).encrypt(field, sessionId)

  private def decrypt(field: EncryptedValue, key: String)(implicit sessionId: String): String =
    SymmetricCryptoFactory.aesGcmAdCrypto(key).decrypt(field, sessionId)
}
