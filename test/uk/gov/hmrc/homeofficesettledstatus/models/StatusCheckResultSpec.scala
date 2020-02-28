package uk.gov.hmrc.homeofficesettledstatus.models

import java.time.LocalDate

import uk.gov.hmrc.play.test.UnitSpec


class StatusCheckResultSpec extends UnitSpec {

  val expectedResult = ImmigrationStatus(LocalDate.parse("2015-02-11"), None, "C", "D", noRecourseToPublicFunds = true )

  "StatusCheckResultSpec" should {


    val formInputWithNoImmigration = StatusCheckResult(
      "A",
      LocalDate.parse("1971-01-01"),
      "B",
      List()
    )

    "return none when there is no immigration status" in {
      formInputWithNoImmigration.mostRecentStatus shouldBe None
    }

    "return the immigration status when there is only one immigration status" in {
      val formInputWithOneImmigrationStatus = StatusCheckResult(
        "A",
        LocalDate.parse("1971-01-01"),
        "B",
        List(expectedResult)
      )

      formInputWithOneImmigrationStatus.mostRecentStatus shouldBe Some(expectedResult)
    }

    "return the most recent immigration status when there is more than one immigration status" in {
      val formInputWithManyImmigrationStatuses = StatusCheckResult(
        "A",
        LocalDate.parse("1971-01-01"),
        "B",
        List(ImmigrationStatus(LocalDate.parse("2010-01-02"), None, "A", "B", noRecourseToPublicFunds = false),
          expectedResult
        )
      )

      formInputWithManyImmigrationStatuses.mostRecentStatus shouldBe Some(expectedResult)


    }
  }







}
