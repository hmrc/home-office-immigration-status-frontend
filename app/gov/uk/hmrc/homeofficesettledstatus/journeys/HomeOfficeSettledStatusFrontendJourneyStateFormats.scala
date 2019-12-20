package gov.uk.hmrc.homeofficesettledstatus.journeys
import play.api.libs.json._
import gov.uk.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State
import gov.uk.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State._
import uk.gov.hmrc.play.fsm.JsonStateFormats

object HomeOfficeSettledStatusFrontendJourneyStateFormats extends JsonStateFormats[State] {

  val EndFormat = Json.format[End]

  override val serializeStateProperties: PartialFunction[State, JsValue] = {
    case s: End => EndFormat.writes(s)
  }

  override def deserializeState(stateName: String, properties: JsValue): JsResult[State] =
    stateName match {
      case "Start"     => JsSuccess(Start)
      case "End"       => EndFormat.reads(properties)
      case "SomeError" => JsSuccess(SomeError)
      case _           => JsError(s"Unknown state name $stateName")
    }
}
