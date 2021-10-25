package uk.gov.hmrc.homeofficeimmigrationstatus.repositories

import play.api.Application
import play.api.test.Injecting
import uk.gov.hmrc.homeofficeimmigrationstatus.support.AppISpec

class SessionCacheRepositorySpec extends AppISpec with Injecting {

  override def fakeApplication: Application = appBuilder.build()

  val sut = inject[SessionCacheRepository]

  "get" must {
    "get a saved query" in {

    }
  }

  "set" must {
    "save a query" in {

    }
  }

  "db" must {
    "have a defined TTL" in {

    }
  }

}
