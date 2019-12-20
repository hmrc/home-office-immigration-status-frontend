package gov.uk.hmrc.homeofficesettledstatus.connectors

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.HttpPost
import gov.uk.hmrc.homeofficesettledstatus.wiring.AppConfig

@Singleton
class FrontendAuthConnector @Inject()(appConfig: AppConfig, val http: HttpPost) extends PlayAuthConnector {

  override val serviceUrl: String = appConfig.authBaseUrl.toString
}
