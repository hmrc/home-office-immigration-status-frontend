package uk.gov.hmrc.homeofficesettledstatus.support

import scala.concurrent.duration._
import akka.util.Timeout
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, Configuration}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import org.scalatest.OptionValues
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.homeofficesettledstatus.config.AppConfig

import scala.language.postfixOps

abstract class NonAuthPageISpec(config: (String, Any)*) extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneServerPerSuite {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config: _*)
    .build()

  implicit val configuration: Configuration = app.configuration
  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty[Lang])
  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val ws: WSClient = app.injector.instanceOf[WSClient]
}
