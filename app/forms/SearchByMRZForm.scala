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

package forms

import forms.SearchByMRZForm._
import models.MrzSearchFormModel
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Singleton

@Singleton
class SearchByMRZForm extends FormFieldMappings {

  def apply(): Form[MrzSearchFormModel] = Form[MrzSearchFormModel] {
    mapping(
      "documentType" -> nonEmptyText("documentType")
        .transform[String](_.toUpperCase, identity)
        .verifying("error.documentType.invalid", AllowedDocumentTypes.contains(_)),
      "documentNumber" -> nonEmptyText("documentNumber")
        .verifying(
          "error.documentNumber.invalid",
          dn => dn.length <= DocumentNumberMaxLength && dn.forall(c => c.isDigit || c.isLetter || c == '-')),
      "dateOfBirth" -> dobFieldsMapping,
      "nationality" -> nonEmptyText("nationality")
        .transform[String](_.toUpperCase, identity)
        .verifying("error.nationality.invalid", CountryList.contains(_))
    )(MrzSearchFormModel.apply)(MrzSearchFormModel.unapply)
  }
}

object SearchByMRZForm {
  final val AllowedDocumentTypes = Seq("PASSPORT", "NAT", "BRC", "BRP")

  final val DocumentNumberMaxLength = 30

  val CountryList = Seq("TODO WHERE ARE WE GETTING THIS FROM", "AFG") //todo iso-3166 list
}
