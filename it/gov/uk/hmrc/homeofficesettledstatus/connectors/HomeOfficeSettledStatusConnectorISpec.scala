package gov.uk.hmrc.homeofficesettledstatus.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http._
import gov.uk.hmrc.homeofficesettledstatus.models.HomeOfficeSettledStatusFrontendModel
import gov.uk.hmrc.homeofficesettledstatus.support.BaseISpec

import scala.concurrent.ExecutionContext.Implicits.global

class HomeOfficeSettledStatusConnectorISpec extends BaseISpec {
  private implicit val hc = HeaderCarrier()

  private lazy val connector: HomeOfficeSettledStatusConnector =
    app.injector.instanceOf[HomeOfficeSettledStatusConnector]

  private val model = HomeOfficeSettledStatusFrontendModel(
    "Dave Agent",
    Some("AA1 1AA"),
    Some("0123456789"),
    Some("email@test.com"))

  "HomeOfficeSettledStatusConnector" when {

    "getSmth" should {

      "return 200" in {
        stubFor(
          get(urlEqualTo(s"/home-office-settled-status/dosmth"))
            .willReturn(aResponse()
              .withStatus(Status.OK)
              .withBody(Json.obj("foo" -> "bar").toString())))

        val response: HttpResponse = await(connector.getSmth())
        response.status shouldBe 200
        verifyTimerExistsAndBeenUpdated("ConsumedAPI-home-office-settled-status-smth-GET")
      }

      "throw an exception if no connection was possible" in {
        stopWireMockServer()
        intercept[BadGatewayException] {
          await(connector.getSmth())
        }
        startWireMockServer()
      }

      "throw an exception if the response is 400" in {
        stubFor(
          get(urlEqualTo(s"/home-office-settled-status/dosmth"))
            .willReturn(aResponse()
              .withStatus(Status.BAD_REQUEST)))

        intercept[BadRequestException] {
          await(connector.getSmth())
        }
      }
    }

    "postSmth" should {

      "return 201" in {
        stubFor(
          post(urlEqualTo(s"/home-office-settled-status/dosmth"))
            .willReturn(aResponse()
              .withStatus(Status.CREATED)))

        val response: HttpResponse = await(connector.postSmth(model))
        response.status shouldBe 201
        verifyTimerExistsAndBeenUpdated("ConsumedAPI-home-office-settled-status-smth-POST")
      }

      "throw an exception if no connection was possible" in {
        stopWireMockServer()
        intercept[BadGatewayException] {
          await(connector.postSmth(model))
        }
        startWireMockServer()
      }

      "throw an exception if the response is 400" in {
        stubFor(
          post(urlEqualTo(s"/home-office-settled-status/dosmth"))
            .willReturn(aResponse()
              .withStatus(Status.BAD_REQUEST)))

        intercept[BadRequestException] {
          await(connector.postSmth(model))
        }
      }
    }
  }

}
