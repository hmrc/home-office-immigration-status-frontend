package stubs

import models.{ImmigrationStatus, StatusCheckResult}

import java.time.LocalDate

trait JourneyTestData {

  val correlationId: String = scala.util.Random.alphanumeric.take(64).mkString

  val expectedResultWithSingleStatus: StatusCheckResult = StatusCheckResult(
    fullName = "Jane Doe",
    dateOfBirth = LocalDate.parse("2001-01-31"),
    nationality = "IRL",
    statuses = List(
      ImmigrationStatus(
        statusStartDate = LocalDate.parse("2018-12-12"),
        statusEndDate = Some(LocalDate.parse("2018-01-31")),
        productType = "EUS",
        immigrationStatus = "ILR",
        noRecourseToPublicFunds = true
      )
    )
  )

  val expectedResultWithMultipleStatuses: StatusCheckResult = StatusCheckResult(
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
}
