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

import com.google.inject.{Inject, Singleton}
import models.{EncryptedFormModel, StatusCheckByNinoFormModel}
import uk.gov.hmrc.domain.Nino
import java.time.LocalDate
import scala.util.Try

@Singleton
class FormModelEncrypter @Inject()(crypto: SecureGCMCipher) {

  def encryptFormModel(formModel: StatusCheckByNinoFormModel, sessionId: String, key: String): EncryptedFormModel = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId, key)

    EncryptedFormModel(
      e(formModel.nino.toString),
      e(formModel.givenName),
      e(formModel.familyName),
      e(formModel.dateOfBirth.toString))
  }

  def decryptFormModel(
    formModel: EncryptedFormModel,
    sessionId: String,
    key: String): Option[StatusCheckByNinoFormModel] = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId, key)

    (for {
      nino      <- Try(Nino(d(formModel.nino)))
      dob       <- Try(LocalDate.parse(d(formModel.dateOfBirth)))
      formModel <- Try(StatusCheckByNinoFormModel(nino, d(formModel.givenName), d(formModel.familyName), dob))
    } yield formModel).toOption
  }

}
