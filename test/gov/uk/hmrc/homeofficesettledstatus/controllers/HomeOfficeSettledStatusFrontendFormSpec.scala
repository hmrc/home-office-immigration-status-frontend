package gov.uk.hmrc.homeofficesettledstatus.controllers

import gov.uk.hmrc.homeofficesettledstatus.models.HomeOfficeSettledStatusFrontendModel
import uk.gov.hmrc.play.test.UnitSpec

class HomeOfficeSettledStatusFrontendFormSpec extends UnitSpec {

  "HomeOfficeSettledStatusFrontendForm" should {

    "bind some input fields and return HomeOfficeSettledStatusFrontendModel and fill it back" in {
      val form = HomeOfficeSettledStatusFrontendController.HomeOfficeSettledStatusFrontendForm

      val value = HomeOfficeSettledStatusFrontendModel(
        name = "SomeValue",
        postcode = None,
        telephoneNumber = None,
        emailAddress = None)

      val fieldValues = Map("name" -> "SomeValue")

      form.bind(fieldValues).value shouldBe Some(value)
      form.fill(value).data shouldBe fieldValues
    }

    "bind all input fields and return HomeOfficeSettledStatusFrontendModel and fill it back" in {
      val form = HomeOfficeSettledStatusFrontendController.HomeOfficeSettledStatusFrontendForm

      val value = HomeOfficeSettledStatusFrontendModel(
        name = "SomeValue",
        postcode = Some("AA1 1AA"),
        telephoneNumber = Some("098765321"),
        emailAddress = Some("foo@bar.com"))

      val fieldValues = Map(
        "name"            -> "SomeValue",
        "postcode"        -> "AA1 1AA",
        "telephoneNumber" -> "098765321",
        "emailAddress"    -> "foo@bar.com")

      form.bind(fieldValues).value shouldBe Some(value)
      form.fill(value).data shouldBe fieldValues
    }
  }
}
