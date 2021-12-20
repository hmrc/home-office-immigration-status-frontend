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

import com.google.inject.Inject
import config.Countries
import forms.SearchByMRZForm._
import models.MrzSearchFormModel
import play.api.data.Form
import play.api.data.Forms.mapping

import javax.inject.Singleton

@Singleton
class SearchByMRZForm @Inject()(countries: Countries) extends FormFieldMappings {

  private val allowedCountries = countries.countries.map(_.alpha3)

  def apply(): Form[MrzSearchFormModel] = Form[MrzSearchFormModel] {
    mapping(
      "documentType" -> nonEmptyText("documentType")
        .transform[String](_.toUpperCase, identity)
        .verifying("error.documentType.invalid", AllowedDocumentTypes.contains(_)),
      "documentNumber" -> nonEmptyText("documentNumber")
        .transform[String](_.replaceAll("\\s", ""), identity)
        .verifying("error.documentNumber.length", dn => dn.length <= DocumentNumberMaxLength)
        .verifying(
          "error.documentNumber.invalid-characters",
          dn => dn.forall(c => c.isDigit || c.isLetter || c == '-')),
      "dateOfBirth" -> dobFieldsMapping,
      "nationality" -> nonEmptyText("nationality")
        .transform[String](_.toUpperCase, identity)
        .verifying("error.nationality.invalid", allowedCountries.contains(_))
    )(MrzSearchFormModel.apply)(MrzSearchFormModel.unapply)
  }
}

object SearchByMRZForm {
  final val AllowedDocumentTypes = Seq("PASSPORT", "NAT", "BRC", "BRP")
  final val DocumentNumberMaxLength = 30
}
