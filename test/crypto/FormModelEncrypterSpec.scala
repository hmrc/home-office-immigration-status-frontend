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

package crypto

import models._
import org.scalatestplus.play.PlaySpec
import utils.NinoGenerator
import uk.gov.hmrc.crypto.{EncryptedValue, SymmetricCryptoFactory}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class FormModelEncrypterSpec extends PlaySpec {

  private val encrypter: FormModelEncrypter = new FormModelEncrypter
  private val secretKey: String             = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val sessionId: String             = "1234567890"

  "encryptNinoSearchFormModel" must {

    "encrypt a nino form model such that it is decrypt-able with the same sessionId and secretKey" in {
      val nino: Nino                     = NinoGenerator.generateNino
      val dateOfBirth: LocalDate         = LocalDate.now().minusYears(1)
      val formModel: NinoSearchFormModel = NinoSearchFormModel(nino, "James", "Buchanan", dateOfBirth)

      val encryptedFormModel: EncryptedSearchFormModel =
        encrypter.encryptSearchFormModel(formModel, sessionId, secretKey)
      val decryptedFormModel: Option[SearchFormModel] =
        encrypter.decryptSearchFormModel(encryptedFormModel, sessionId, secretKey)

      decryptedFormModel must be(Some(formModel))
    }

    "encrypt an mrz form model such that it is decrypt-able with the same sessionId and secretKey" in {
      val dateOfBirth: LocalDate = LocalDate.now().minusYears(1)
      val formModel: MrzSearchFormModel =
        MrzSearchFormModel("documentType", "documentNumber", dateOfBirth, "nationality")

      val encryptedFormModel: EncryptedSearchFormModel =
        encrypter.encryptSearchFormModel(formModel, sessionId, secretKey)
      val decryptedFormModel: Option[SearchFormModel] =
        encrypter.decryptSearchFormModel(encryptedFormModel, sessionId, secretKey)

      decryptedFormModel must be(Some(formModel))
    }
  }

  "decryptNinoSearchFormModel" must {

    "return None where the nino is invalid for a nino model" in {
      val nino: Nino                     = NinoGenerator.generateNino
      val dateOfBirth: LocalDate         = LocalDate.now().minusYears(1)
      val formModel: NinoSearchFormModel = NinoSearchFormModel(nino, "James", "Buchanan", dateOfBirth)

      val encryptedWithBadNino: EncryptedNinoSearchFormModel = EncryptedNinoSearchFormModel(
        encrypt("abc123"),
        encrypt(formModel.givenName),
        encrypt(formModel.familyName),
        encrypt(formModel.dateOfBirth.toString)
      )

      val decryptedFormModel: Option[SearchFormModel] =
        encrypter.decryptSearchFormModel(encryptedWithBadNino, sessionId, secretKey)

      decryptedFormModel must be(None)
    }

    "return None where the dob is invalid for a nino model" in {
      val nino: Nino                     = NinoGenerator.generateNino
      val dateOfBirth: LocalDate         = LocalDate.now().minusYears(1)
      val formModel: NinoSearchFormModel = NinoSearchFormModel(nino, "James", "Buchanan", dateOfBirth)

      val encryptedWithBadDob: EncryptedNinoSearchFormModel =
        EncryptedNinoSearchFormModel(
          encrypt(formModel.nino.toString),
          encrypt(formModel.givenName),
          encrypt(formModel.familyName),
          encrypt("123456")
        )

      val decryptedFormModel: Option[SearchFormModel] =
        encrypter.decryptSearchFormModel(encryptedWithBadDob, sessionId, secretKey)

      decryptedFormModel must be(None)
    }

    "return None where the dob is invalid an mrz model" in {
      val dateOfBirth: LocalDate = LocalDate.now().minusYears(1)
      val formModel: MrzSearchFormModel =
        MrzSearchFormModel("documentType", "documentNumber", dateOfBirth, "nationality")

      val encryptedWithBadDob: EncryptedMrzSearchFormModel =
        EncryptedMrzSearchFormModel(
          encrypt(formModel.documentType),
          encrypt(formModel.documentNumber),
          encrypt("123456"),
          encrypt(formModel.nationality)
        )

      val decryptedFormModel: Option[SearchFormModel] =
        encrypter.decryptSearchFormModel(encryptedWithBadDob, sessionId, secretKey)

      decryptedFormModel must be(None)
    }
  }

  private def encrypt(field: String): EncryptedValue =
    SymmetricCryptoFactory.aesGcmAdCrypto(secretKey).encrypt(field, sessionId)
}
