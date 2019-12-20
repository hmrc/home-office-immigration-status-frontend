package gov.uk.hmrc.homeofficesettledstatus.journey
import uk.gov.hmrc.http.HeaderCarrier
import gov.uk.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State.{End, Start}
import gov.uk.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.Transitions._
import gov.uk.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.{State, Transition}
import gov.uk.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyService
import gov.uk.hmrc.homeofficesettledstatus.models.HomeOfficeSettledStatusFrontendModel
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeSettledStatusFrontendModelSpec extends UnitSpec with StateMatchers[State] {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  case class given(initialState: State)
      extends HomeOfficeSettledStatusFrontendJourneyService with TestStorage[(State, List[State])] {
    await(save((initialState, Nil)))

    def when(transition: Transition): (State, List[State]) =
      await(super.apply(transition))
  }

  "HomeOfficeSettledStatusFrontendModel" when {
    "at state Start" should {
      "transition to End when Start submitted a form" in {
        given(Start) when submitStart("001.H")(HomeOfficeSettledStatusFrontendModel("Henry", None, None, None)) should thenGo(
          End("Henry", None, None, None))
      }
    }
  }
}
