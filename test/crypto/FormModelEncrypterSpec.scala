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

package crypto

import models.{EncryptedFormModel, StatusCheckByNinoFormModel}
import org.scalatestplus.play.PlaySpec
import utils.NinoGenerator
import java.time.LocalDate

class FormModelEncrypterSpec extends PlaySpec {

  private val cipher = new SecureGCMCipherImpl
  private val encrypter = new FormModelEncrypter(cipher)
  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val sessionId = "1234567890"

  "encryptFormModel" must {

    "encrypt a form model such that it is decryptable with the same sessionId and secretKey" in {
      val nino = NinoGenerator.generateNino
      val dateOfBirth = LocalDate.now().minusYears(1)
      val formModel = StatusCheckByNinoFormModel(nino, "James", "Buchanan", dateOfBirth)

      val encryptedFormModel = encrypter.encryptFormModel(formModel, sessionId, secretKey)
      val decryptedFormModel = encrypter.decryptFormModel(encryptedFormModel, sessionId, secretKey)

      decryptedFormModel mustEqual Some(formModel)
    }
  }

  "decryptFormModel" must {

    "return None where the nino is invalid" in {
      val nino = NinoGenerator.generateNino
      val dateOfBirth = LocalDate.now().minusYears(1)
      val formModel = StatusCheckByNinoFormModel(nino, "James", "Buchanan", dateOfBirth)

      def e(field: String): EncryptedValue = cipher.encrypt(field, sessionId, secretKey)

      val encryptedWithBadNino = EncryptedFormModel(
        e("abc123"),
        e(formModel.givenName),
        e(formModel.familyName),
        e(formModel.dateOfBirth.toString))

      val decryptedFormModel = encrypter.decryptFormModel(encryptedWithBadNino, sessionId, secretKey)

      decryptedFormModel mustEqual None
    }

    "return None where the dob is invalid" in {
      val nino = NinoGenerator.generateNino
      val dateOfBirth = LocalDate.now().minusYears(1)
      val formModel = StatusCheckByNinoFormModel(nino, "James", "Buchanan", dateOfBirth)

      def e(field: String): EncryptedValue = cipher.encrypt(field, sessionId, secretKey)

      val encryptedWithBadDob =
        EncryptedFormModel(e(formModel.nino.toString), e(formModel.givenName), e(formModel.familyName), e("123456"))

      val decryptedFormModel = encrypter.decryptFormModel(encryptedWithBadDob, sessionId, secretKey)

      decryptedFormModel mustEqual None
    }

  }

}
