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

package uk.gov.hmrc.homeofficeimmigrationstatus.forms

import uk.gov.hmrc.homeofficeimmigrationstatus.models.{StatusCheckByNinoRequest, StatusCheckRange}
import uk.gov.hmrc.domain.Nino
import play.api.data.Form
import play.api.data.Forms._
import javax.inject.{Inject, Singleton}

class StatusCheckByNinoFormProvider @Inject() extends FormFieldMappings {

  def apply(): Form[StatusCheckByNinoRequest] = Form[StatusCheckByNinoRequest](
    mapping(
      "nino" -> uppercaseNormalizedText
        .verifying(validNino())
        .transform(Nino.apply, (n: Nino) => n.toString),
      "givenName"   -> trimmedName.verifying(validName("givenName", 1)),
      "familyName"  -> trimmedName.verifying(validName("familyName", 2)),
      "dateOfBirth" -> dateOfBirthMapping,
      "range"       -> ignored[Option[StatusCheckRange]](None)
    )(StatusCheckByNinoRequest.apply)(StatusCheckByNinoRequest.unapply)
  )
}
