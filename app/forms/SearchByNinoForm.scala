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

package forms

import models.NinoSearchFormModel
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Singleton

@Singleton
class SearchByNinoForm extends FormFieldMappings {

  def apply(): Form[NinoSearchFormModel] = Form[NinoSearchFormModel] {
    mapping(
      "nino"        -> validNino,
      "givenName"   -> validName("givenName", 1),
      "familyName"  -> validName("familyName", 2),
      "dateOfBirth" -> dobFieldsMapping
    )(NinoSearchFormModel.apply)(NinoSearchFormModel.unapply)
  }
}
