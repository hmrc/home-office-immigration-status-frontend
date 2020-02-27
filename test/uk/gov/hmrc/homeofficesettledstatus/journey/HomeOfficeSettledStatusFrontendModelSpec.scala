/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.State._
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.Transitions._
import uk.gov.hmrc.homeofficesettledstatus.journeys.HomeOfficeSettledStatusFrontendJourneyModel.{State, Transition, TransitionNotAllowed}
import uk.gov.hmrc.homeofficesettledstatus.models._
import uk.gov.hmrc.homeofficesettledstatus.services.HomeOfficeSettledStatusFrontendJourneyService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HomeOfficeSettledStatusFrontendModelSpec
    extends UnitSpec with StateMatchers[State] with TestData {

  // dummy journey context
  case class DummyContext()
  implicit val dummyContext: DummyContext = DummyContext()

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
          given(Start) when submitStatusCheckByNino(checkStatusByNino)(userId)(validQuery)
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
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino)(userId)(validQuery) should thenGo(
          StatusFound(correlationId, validQuery, expectedResult))
      }
      "transition to StatusFound when submitStatusCheckByNino with valid query with date range" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino)(userId)(
          validQueryWithDateRange) should thenGo(
          StatusFound(correlationId, validQueryWithDateRange, expectedResult))
      }
      "transition to StatusCheckFailure when submitStatusCheckByNino with invalid query" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino)(userId)(invalidQuery) should thenGo(
          StatusCheckFailure(correlationId, invalidQuery, errorNotFound))
      }
      "transition to StatusCheckFailure when submitStatusCheckByNino returns strange response" in {
        atStatusCheckByNino when submitStatusCheckByNino(checkStatusByNino)(userId)(strangeQuery) should thenGo(
          StatusCheckFailure(correlationId, strangeQuery, errorUnknown))
      }
    }

    "at state StatusFound" should {

      def atStatusFound =
        given(StatusFound(correlationId, validQuery, expectedResult))
          .withBreadcrumbs(StatusCheckByNino(), Start)

      "transition to Start when start" in {
        atStatusFound when start(userId) should thenGo(Start)
      }
      "go to StatusCheckByNino when showStatusCheckByNino" in {
        atStatusFound when showStatusCheckByNino(userId) should thenGo(StatusCheckByNino())
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with valid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusFound when submitStatusCheckByNino(checkStatusByNino)(userId)(validQuery)
        }
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with invalid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusFound when submitStatusCheckByNino(checkStatusByNino)(userId)(invalidQuery)
        }
      }
    }

    "at state StatusCheckFailure" should {

      def atStatusCheckFailure =
        given(StatusCheckFailure(correlationId, invalidQuery, errorNotFound))
          .withBreadcrumbs(StatusCheckByNino(), Start)

      "transition to Start when start" in {
        atStatusCheckFailure when start(userId) should thenGo(Start)
      }
      "go back to StatusCheckByNino when showStatusCheckByNino" in {
        atStatusCheckFailure when showStatusCheckByNino(userId) should thenGo(
          StatusCheckByNino(Some(invalidQuery)))
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with valid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusCheckFailure when submitStatusCheckByNino(checkStatusByNino)(userId)(validQuery)
        }
      }
      "throw an exception at StatusFound when submitStatusCheckByNino with invalid query" in {
        an[TransitionNotAllowed] shouldBe thrownBy {
          atStatusCheckFailure when submitStatusCheckByNino(checkStatusByNino)(userId)(invalidQuery)
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

  val validQuery =
    StatusCheckByNinoRequest("2001-01-31", "JANE", "DOE", Nino("RJ301829A"))

  val validQueryWithDateRange =
    StatusCheckByNinoRequest(
      "2001-01-31",
      "JANE",
      "DOE",
      Nino("RJ301829A"),
      Some(
        StatusCheckRange(Some(LocalDate.now().minusDays(6)), Some(LocalDate.now().minusMonths(6)))))

  val strangeQuery =
    StatusCheckByNinoRequest("1999-12-31", "FOO", "BAR", Nino("KA339738D"))

  val invalidQuery =
    StatusCheckByNinoRequest("1982-12-12", "MARIA", "DOLL", Nino("AB888330D"))

  val expectedResult = StatusCheckResult(
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

  val errorNotFound = StatusCheckError(errCode = "ERR_NOT_FOUND")
  val errorUnknown = StatusCheckError(errCode = "ERR_UNKNOWN")

  val checkStatusByNino: StatusCheckByNinoRequest => Future[StatusCheckResponse] =
    (request: StatusCheckByNinoRequest) =>
      Future.successful(
        if (request.nino == validQuery.nino)
          StatusCheckResponse(correlationId = correlationId, result = Some(expectedResult))
        else if (request.nino == strangeQuery.nino)
          StatusCheckResponse(correlationId = correlationId)
        else
          StatusCheckResponse(correlationId = correlationId, error = Some(errorNotFound)))

}
