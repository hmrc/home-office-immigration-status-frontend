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

package uk.gov.hmrc.homeofficesettledstatus.journey

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State._
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.Transitions._
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.{State, Transition, TransitionNotAllowed}
import uk.gov.hmrc.homeofficesettledstatus.models._
import uk.gov.hmrc.homeofficesettledstatus.services.HomeOfficeSettledStatusFrontendJourneyService
import uk.gov.hmrc.homeofficesettledstatus.support.{InMemoryStore, StateMatchers}
import org.scalatest.OptionValues
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HomeOfficeSettledStatusFrontendModelSpec
    extends AnyWordSpecLike with Matchers with OptionValues with StateMatchers[State] with TestData {

  // dummy journey context
  case class DummyContext()
  implicit val dummyContext: DummyContext = DummyContext()

  val queryMonths = 6

  "HomeOfficeSettledStatusFrontendModel" when {

    "at state Start" should {

      "stay at Start when start" in {
        given(Start) when start(userId) should thenGo(Start)
      }
      "transition to StatusCheckByNino when showStatusCheckByNino" in {
        given(Start) when showStatusCheckByNino(userId) should thenGo(StatusCheckByNino())
      }
      "throw an exception at Start when submitStatusCheckByNino" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          given(Start) when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningMatch)
        }
      }
    }

    "at state StatusCheckByNino" should {

      def atStatusCheckByNino = given(StatusCheckByNino()).withBreadcrumbs(Start)

      "transition to Start when start" in {
        atStatusCheckByNino when start(userId) should thenGo(Start)
      }
      "stay at StatusCheckByNino when showStatusCheckByNino" in {
        atStatusCheckByNino when showStatusCheckByNino(userId) should thenGo(StatusCheckByNino())
      }
      "transition to StatusFound when submitStatusCheckByNino with valid query" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningMatch) should thenGo(
          StatusFound(correlationId, queryReturningMatch, matchFoundResult))
      }
      "transition to StatusFound when submitStatusCheckByNino with valid query with date range" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningMatchDateRange) should thenGo(
          StatusFound(correlationId, queryReturningMatchDateRange, matchFoundResult))
      }
      "transition to StatusNotAvailable when submitStatusCheckByNino with valid query with date range" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningEmptyResponse) should
          thenGo(StatusNotAvailable(correlationId, queryReturningEmptyResponse))
      }
      "transition to StatusCheckFailure when submitStatusCheckByNino with invalid query" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningUnsupportedMatch) should thenGo(
          StatusCheckFailure(correlationId, queryReturningUnsupportedMatch, errorUnsupportedStatus))
      }
      "transition to StatusNotAvailable when submitStatusCheckByNino returns empty response" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningEmptyResponse) should thenGo(StatusNotAvailable(correlationId, queryReturningEmptyResponse))
      }
      "transition to StatusNotAvailable when submitStatusCheckByNino returns empty status list" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningEmptyStatusList) should thenGo(StatusNotAvailable(correlationId, queryReturningEmptyStatusList))
      }
    }

    "at state StatusFound" should {

      def atStatusFound =
        given(StatusFound(correlationId, queryReturningMatch, matchFoundResult))
          .withBreadcrumbs(StatusCheckByNino(), Start)

      "transition to Start when start" in {
        atStatusFound when start(userId) should thenGo(Start)
      }
      "go to StatusCheckByNino when showStatusCheckByNino" in {
        atStatusFound when showStatusCheckByNino(userId) should thenGo(StatusCheckByNino())
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with valid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusFound when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningMatch)
        }
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with invalid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusFound when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningNoMatch)
        }
      }
    }

    "at state StatusNotAvailable" should {

      def atStatusNotAvailable =
        given(StatusNotAvailable(correlationId, queryReturningEmptyResponse))
          .withBreadcrumbs(StatusCheckByNino(), Start)

      "transition to Start when start" in {
        atStatusNotAvailable when start(userId) should thenGo(Start)
      }
      "go to StatusCheckByNino when showStatusCheckByNino" in {
        atStatusNotAvailable when showStatusCheckByNino(userId) should thenGo(StatusCheckByNino())
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with valid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusNotAvailable when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningMatch)
        }
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with invalid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusNotAvailable when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
            queryReturningNoMatch)
        }
      }
    }

    "at state StatusCheckFailure" should {

      def atStatusCheckFailure =
        given(StatusCheckFailure(correlationId, queryReturningNoMatch, errorNotFound))
          .withBreadcrumbs(StatusCheckByNino(), Start)

      "transition to Start when start" in {
        atStatusCheckFailure when start(userId) should thenGo(Start)
      }
      "go back to StatusCheckByNino when showStatusCheckByNino" in {
        atStatusCheckFailure when showStatusCheckByNino(userId) should thenGo(
          StatusCheckByNino(Some(queryReturningNoMatch)))
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with valid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusCheckFailure when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningMatch)
        }
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with invalid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusCheckFailure when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
            queryReturningNoMatch)
        }
      }
    }
  }

  case class given(initialState: State)
      extends HomeOfficeSettledStatusFrontendJourneyService[DummyContext]
      with InMemoryStore[(State, List[State]), DummyContext] {

    await(save((initialState, Nil)))

    def withBreadcrumbs(breadcrumbs: State*): this.type = {
      val (state, _) = await(fetch).getOrElse((Start, Nil))
      await(save((state, breadcrumbs.toList)))
      this
    }

    def when(transition: Transition): (State, List[State]) =
      await(super.apply(transition))
  }
}

trait TestData {

  val userId = "foo"
  val correlationId = "123"

  val queryReturningMatch =
    StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")

  val queryReturningMatchDateRange =
    StatusCheckByNinoRequest(
      Nino("RJ301829A"),
      "Doe",
      "Jane",
      "2001-01-31",
      Some(StatusCheckRange(Some(LocalDate.now().minusDays(6)), Some(LocalDate.now().minusMonths(6)))))

  val queryReturningEmptyResponse =
    StatusCheckByNinoRequest(Nino("KA339738D"), "BAR", "FOO", "1997-10-29")

  val queryReturningEmptyStatusList =
    StatusCheckByNinoRequest(Nino("SR137010A"), "FOO", "BAR", "1998-11-30")

  val queryReturningUnsupportedMatch =
    StatusCheckByNinoRequest(Nino("BS088353B"), "BAR", "FOO", "1999-12-31")

  val queryReturningNoMatch =
    StatusCheckByNinoRequest(Nino("AB888330D"), "DOLL", "MARIA", "1982-12-12")

  val matchFoundResult = StatusCheckResult(
    fullName = "Jane Doe",
    dateOfBirth = LocalDate.parse("2001-01-31"),
    nationality = "IRL",
    statuses = List(
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2018-12-12"),
        productType = "EUS",
        immigrationStatus = "ILR",
        noRecourseToPublicFunds = true
      ),
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2018-01-01"),
        statusEndDate = Some(LocalDate.parse("2018-12-11")),
        productType = "EUS",
        immigrationStatus = "LTR",
        noRecourseToPublicFunds = false
      )
    )
  )

  val unsupportedMatchFoundResult = StatusCheckResult(
    fullName = "Jane Doe",
    dateOfBirth = LocalDate.parse("2001-01-31"),
    nationality = "IRL",
    statuses = List(
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2018-12-12"),
        productType = "FOO",
        immigrationStatus = "FOO",
        noRecourseToPublicFunds = true
      )
    )
  )

  val errorNotFound = StatusCheckError(errCode = "ERR_NOT_FOUND")
  val errorUnsupportedStatus = StatusCheckError(errCode = "UNSUPPORTED_STATUS")

  val checkStatusByNino: StatusCheckByNinoRequest => Future[StatusCheckResponse] =
    (request: StatusCheckByNinoRequest) =>
      Future.successful(
        if (request.nino == queryReturningMatch.nino)
          StatusCheckResponse(correlationId = correlationId, result = Some(matchFoundResult))
        else if (request.nino == queryReturningEmptyResponse.nino)
          StatusCheckResponse(correlationId = correlationId)
        else if (request.nino == queryReturningEmptyStatusList.nino)
          StatusCheckResponse(correlationId = correlationId, result = Some(matchFoundResult.copy(statuses = List())))
        else if (request.nino == queryReturningUnsupportedMatch.nino)
          StatusCheckResponse(correlationId = correlationId, result = Some(unsupportedMatchFoundResult))
        else
          StatusCheckResponse(correlationId = correlationId, error = Some(errorNotFound)))

}
