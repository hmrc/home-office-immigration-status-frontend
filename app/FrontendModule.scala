import gov.uk.hmrc.homeofficesettledstatus.connectors.FrontendAuthConnector
import com.google.inject.AbstractModule
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class FrontendModule(val environment: Environment, val configuration: Configuration) extends AbstractModule {

  def configure(): Unit = {
    val appName = "new-shiny-service-26-frontend"
    Logger(getClass).info(s"Starting microservice : $appName : in mode : ${environment.mode}")

    bind(classOf[HttpGet]).to(classOf[DefaultHttpClient])
    bind(classOf[HttpPost]).to(classOf[DefaultHttpClient])
    bind(classOf[AuthConnector]).to(classOf[FrontendAuthConnector])
  }
}
