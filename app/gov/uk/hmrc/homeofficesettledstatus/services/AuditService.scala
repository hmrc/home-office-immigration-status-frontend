package gov.uk.hmrc.homeofficesettledstatus.services

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.mvc.Request
import uk.gov.hmrc.agentmtdidentifiers.model.Arn
import uk.gov.hmrc.http.HeaderCarrier
import gov.uk.hmrc.homeofficesettledstatus.models.HomeOfficeSettledStatusFrontendModel
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object HomeOfficeSettledStatusFrontendEvent extends Enumeration {
  val HomeOfficeSettledStatusFrontendSomethingHappened = Value
  type HomeOfficeSettledStatusFrontendEvent = Value
}

@Singleton
class AuditService @Inject()(val auditConnector: AuditConnector) {

  import HomeOfficeSettledStatusFrontendEvent._

  def sendHomeOfficeSettledStatusFrontendSomethingHappened(
    model: HomeOfficeSettledStatusFrontendModel,
    agentReference: Arn)(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): Unit =
    auditEvent(
      HomeOfficeSettledStatusFrontendEvent.HomeOfficeSettledStatusFrontendSomethingHappened,
      "new-shiny-service-26-frontend-something-happened",
      Seq(
        "agentReference"  -> agentReference.value,
        "name"            -> model.name,
        "telephoneNumber" -> model.telephoneNumber.getOrElse(""),
        "emailAddress"    -> model.emailAddress.getOrElse("")
      )
    )

  private[services] def auditEvent(
    event: HomeOfficeSettledStatusFrontendEvent,
    transactionName: String,
    details: Seq[(String, Any)] = Seq.empty)(
    implicit hc: HeaderCarrier,
    request: Request[Any],
    ec: ExecutionContext): Future[Unit] =
    send(createEvent(event, transactionName, details: _*))

  private[services] def createEvent(
    event: HomeOfficeSettledStatusFrontendEvent,
    transactionName: String,
    details: (String, Any)*)(implicit hc: HeaderCarrier, request: Request[Any], ec: ExecutionContext): DataEvent = {

    val detail = hc.toAuditDetails(details.map(pair => pair._1 -> pair._2.toString): _*)
    val tags = hc.toAuditTags(transactionName, request.path)
    DataEvent(auditSource = "new-shiny-service-26-frontend", auditType = event.toString, tags = tags, detail = detail)
  }

  private[services] def send(events: DataEvent*)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    Future {
      events.foreach { event =>
        Try(auditConnector.sendEvent(event))
      }
    }

}
