package uk.gov.hmrc.homeofficesettledstatus.support

import scala.concurrent.duration._

import akka.util.Timeout
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, Configuration}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import org.scalatest.{Matchers, OptionValues, WordSpecLike}

abstract class NonAuthPageISpec(config: (String, Any)*) extends WordSpecLike with Matchers with OptionValues with GuiceOneServerPerSuite {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config: _*)
    .build()

  implicit val configuration: Configuration = app.configuration
  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty[Lang])
  implicit val timeout: Timeout = Timeout(5 seconds)

  val ws = app.injector.instanceOf[WSClient]
}
