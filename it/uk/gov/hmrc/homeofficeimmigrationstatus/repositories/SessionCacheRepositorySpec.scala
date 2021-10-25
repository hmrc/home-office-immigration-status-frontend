package uk.gov.hmrc.homeofficeimmigrationstatus.repositories

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting

class SessionCacheRepositorySpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  lazy val sut: SessionCacheRepository = inject[SessionCacheRepository]

  "db" must {
    "have a defined TTL" in {
      val ttlIndex = sut.indexes.find(_.name.contains("lastUpdatedTTL"))

      ttlIndex mustBe defined
      assert(ttlIndex.get.options.contains("expireAfterSeconds"))
    }
  }

}
