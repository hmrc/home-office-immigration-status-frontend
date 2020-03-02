package uk.gov.hmrc.homeofficesettledstatus.support

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.{ExecutionContext, Future}

/**
  * Basic in-memory store used to test journeys.
  */
trait InMemoryStore[S, C] {

  private val state = new AtomicReference[Option[S]](None)

  def fetch(implicit requestContext: C, ec: ExecutionContext): Future[Option[S]] =
    Future.successful(state.get)

  def save(newState: S)(implicit requestContext: C, ec: ExecutionContext): Future[S] = Future {
    state.set(Some(newState))
    newState
  }
}
