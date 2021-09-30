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

class HomeOfficeSettledStatusFrontendJourneyModelSpec
    extends AnyWordSpecLike with Matchers with OptionValues with StateMatchers[State] with TestData {

  // dummy journey context
  case class DummyContext()
  implicit val dummyContext: DummyContext = DummyContext()

  val queryMonths = 6

  "HomeOfficeSettledStatusFrontendJourneyModel" when {

    "at state Start" should {

      "stay at Start when start" in {
        given(Start) when start(userId) should thenGo(Start)
      }
      "transition to StatusCheckByNino when showStatusCheckByNino" in {
        given(Start) when showStatusCheckByNino(userId) should thenGo(StatusCheckByNino())
      }
      "throw an exception at Start when submitStatusCheckByNino" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          given(Start) when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningEUSMatch)
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
      "transition to StatusFound when submitStatusCheckByNino with valid EUS query" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningEUSMatch) should thenGo(
          StatusFound(correlationId, queryReturningEUSMatch, eusMatchFoundResult))
      }
      "transition to StatusFound when submitStatusCheckByNino with valid non-EUS query" in {
        atStatusCheckByNino when
          submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningNonEUSMatch) should thenGo(
          StatusFound(correlationId, queryReturningNonEUSMatch, nonEUSMatchFoundResult))
      }
      "transition to StatusFound when submitStatusCheckByNino with valid unknown status query" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningUnknownMatch) should thenGo(
          StatusFound(correlationId, queryReturningUnknownMatch, unknownMatchFoundResult))
      }
      "transition to StatusFound when submitStatusCheckByNino with valid query with date range" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningMatchDateRange) should thenGo(
          StatusFound(correlationId, queryReturningMatchDateRange, eusMatchFoundResult))
      }
      "transition to StatusNotAvailable when submitStatusCheckByNino with valid query with date range" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
          queryReturningEmptyResponse) should
          thenGo(StatusNotAvailable(correlationId, queryReturningEmptyResponse))
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
        given(StatusFound(correlationId, queryReturningEUSMatch, eusMatchFoundResult))
          .withBreadcrumbs(StatusCheckByNino(), Start)

      "transition to Start when start" in {
        atStatusFound when start(userId) should thenGo(Start)
      }
      "go to StatusCheckByNino when showStatusCheckByNino" in {
        atStatusFound when showStatusCheckByNino(userId) should thenGo(StatusCheckByNino())
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with valid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusFound when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(queryReturningEUSMatch)
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
          atStatusNotAvailable when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
            queryReturningEUSMatch)
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
          atStatusCheckFailure when submitStatusCheckByNino(checkStatusByNino, queryMonths)(userId)(
            queryReturningEUSMatch)
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

  val queryReturningEUSMatch =
    StatusCheckByNinoRequest(Nino("RJ301829A"), "Doe", "Jane", "2001-01-31")

  val queryReturningNonEUSMatch =
    StatusCheckByNinoRequest(Nino("RJ123321A"), "Johnson", "Dave", "2001-02-28")

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

  val queryReturningUnknownMatch =
    StatusCheckByNinoRequest(Nino("BS088353B"), "BAR", "FOO", "1999-12-31")

  val queryReturningNoMatch =
    StatusCheckByNinoRequest(Nino("AB888330D"), "DOLL", "MARIA", "1982-12-12")

  val eusMatchFoundResult = StatusCheckResult(
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

  val nonEUSMatchFoundResult = StatusCheckResult(
    fullName = "Dave Johnson",
    dateOfBirth = LocalDate.parse("2001-02-28"),
    nationality = "JPN",
    statuses = List(
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2021-01-12"),
        productType = "WORK",
        immigrationStatus = "LTR",
        noRecourseToPublicFunds = false
      )
    )
  )

  val unknownMatchFoundResult = StatusCheckResult(
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

  val resultMap: Map[Nino, Option[StatusCheckResult]] = Map(
    queryReturningEUSMatch.nino        -> Some(eusMatchFoundResult),
    queryReturningNonEUSMatch.nino     -> Some(nonEUSMatchFoundResult),
    queryReturningEmptyResponse.nino   -> None,
    queryReturningEmptyStatusList.nino -> Some(eusMatchFoundResult.copy(statuses = List())),
    queryReturningUnknownMatch.nino    -> Some(unknownMatchFoundResult)
  )

  val checkStatusByNino: StatusCheckByNinoRequest => Future[StatusCheckResponse] =
    (request: StatusCheckByNinoRequest) =>
      resultMap.get(request.nino) match {
        case Some(result) => Future.successful(StatusCheckResponse(correlationId = correlationId, result = result))
        case None         => Future.successful(StatusCheckResponse(correlationId = correlationId, error = Some(errorNotFound)))
    }
}
