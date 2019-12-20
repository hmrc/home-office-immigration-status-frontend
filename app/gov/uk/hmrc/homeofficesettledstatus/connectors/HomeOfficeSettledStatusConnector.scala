package gov.uk.hmrc.homeofficesettledstatus.connectors

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import gov.uk.hmrc.homeofficesettledstatus.models.HomeOfficeSettledStatusFrontendModel
import gov.uk.hmrc.homeofficesettledstatus.wiring.AppConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeOfficeSettledStatusConnector @Inject()(appConfig: AppConfig, http: HttpGet with HttpPost, metrics: Metrics)
    extends HttpAPIMonitor {

  val baseUrl: String = appConfig.serviceBaseUrl

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def getSmth()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    monitor(s"ConsumedAPI-home-office-settled-status-smth-GET") {
      http.GET[HttpResponse](new URL(baseUrl + "/home-office-settled-status/dosmth").toExternalForm)
    }

  def postSmth(model: HomeOfficeSettledStatusFrontendModel)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[HttpResponse] =
    monitor(s"ConsumedAPI-home-office-settled-status-smth-POST") {
      http.POST[HomeOfficeSettledStatusFrontendModel, HttpResponse](
        new URL(baseUrl + "/home-office-settled-status/dosmth").toExternalForm,
        model)
    }

}
