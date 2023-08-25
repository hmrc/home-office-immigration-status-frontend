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

import java.time.LocalDate

import com.google.inject.Singleton
import models._
import uk.gov.hmrc.crypto.{EncryptedValue, SymmetricCryptoFactory}
import uk.gov.hmrc.domain.Nino

import scala.util.Try

@Singleton
class FormModelEncrypter {

  def encryptSearchFormModel(formModel: SearchFormModel, sessionId: String, key: String): EncryptedSearchFormModel =
    formModel match {
      case model: NinoSearchFormModel => encryptNinoSearchFormModel(model, sessionId, key)
      case model: MrzSearchFormModel  => encryptMrzSearchFormModel(model, sessionId, key)
    }

  def decryptSearchFormModel(
    formModel: EncryptedSearchFormModel,
    sessionId: String,
    key: String
  ): Option[SearchFormModel] =
    formModel match {
      case model: EncryptedNinoSearchFormModel => decryptNinoSearchFormModel(model, sessionId, key)
      case model: EncryptedMrzSearchFormModel  => decryptMrzSearchFormModel(model, sessionId, key)
    }

  private def encryptNinoSearchFormModel(
    formModel: NinoSearchFormModel,
    sessionId: String,
    key: String
  ): EncryptedNinoSearchFormModel = {
    def e(field: String): EncryptedValue = SymmetricCryptoFactory.aesGcmAdCrypto(key).encrypt(field, sessionId)
    EncryptedNinoSearchFormModel(
      e(formModel.nino.toString),
      e(formModel.givenName),
      e(formModel.familyName),
      e(formModel.dateOfBirth.toString)
    )
  }

  private def decryptNinoSearchFormModel(
    formModel: EncryptedNinoSearchFormModel,
    sessionId: String,
    key: String
  ): Option[NinoSearchFormModel] = {
    def d(field: EncryptedValue): String = SymmetricCryptoFactory.aesGcmAdCrypto(key).decrypt(field, sessionId)

    (for {
      nino      <- Try(Nino(d(formModel.nino)))
      dob       <- Try(LocalDate.parse(d(formModel.dateOfBirth)))
      formModel <- Try(NinoSearchFormModel(nino, d(formModel.givenName), d(formModel.familyName), dob))
    } yield formModel).toOption
  }

  private def encryptMrzSearchFormModel(
    formModel: MrzSearchFormModel,
    sessionId: String,
    key: String
  ): EncryptedMrzSearchFormModel = {
    def e(field: String): EncryptedValue = SymmetricCryptoFactory.aesGcmAdCrypto(key).encrypt(field, sessionId)

    EncryptedMrzSearchFormModel(
      e(formModel.documentType),
      e(formModel.documentNumber),
      e(formModel.dateOfBirth.toString),
      e(formModel.nationality)
    )
  }

  private def decryptMrzSearchFormModel(
    formModel: EncryptedMrzSearchFormModel,
    sessionId: String,
    key: String
  ): Option[MrzSearchFormModel] = {
    def d(field: EncryptedValue): String = SymmetricCryptoFactory.aesGcmAdCrypto(key).decrypt(field, sessionId)

    (for {
      dob <- Try(LocalDate.parse(d(formModel.dateOfBirth)))
      formModel <-
        Try(MrzSearchFormModel(d(formModel.documentType), d(formModel.documentNumber), dob, d(formModel.nationality)))
    } yield formModel).toOption
  }
}
