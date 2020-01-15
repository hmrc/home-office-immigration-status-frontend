package uk.gov.hmrc.homeofficesettledstatus.connectors

import uk.gov.hmrc.homeofficesettledstatus.wiring.AppConfig

case class TestAppConfig(wireMockBaseUrl: String, wireMockPort: Int) extends AppConfig {

  override val appName: String = "home-office-settled-status-frontend"
  override val authBaseUrl: String = wireMockBaseUrl
  override val homeOfficeSettledStatusProxyBaseUrl: String = wireMockBaseUrl
  override val mongoSessionExpiryTime: Int = 3600
  override val authorisedStrideGroup: String = "TBC"
}
