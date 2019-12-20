package gov.uk.hmrc.homeofficesettledstatus.models

import play.api.libs.json.Json

case class HomeOfficeSettledStatusFrontendModel(
  name: String,
  postcode: Option[String],
  telephoneNumber: Option[String],
  emailAddress: Option[String])

object HomeOfficeSettledStatusFrontendModel {
  implicit val modelFormat = Json.format[HomeOfficeSettledStatusFrontendModel]
}
