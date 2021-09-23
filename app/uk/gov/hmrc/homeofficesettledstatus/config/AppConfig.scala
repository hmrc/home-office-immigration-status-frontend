/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.homeofficesettledstatus.config

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import javax.inject.{Inject, Singleton}

import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig) {

  val appName: String = servicesConfig.getString("appName")
  val authBaseUrl: String = servicesConfig.baseUrl("auth")
  val homeOfficeSettledStatusProxyBaseUrl: String = servicesConfig.baseUrl("home-office-settled-status-proxy")
  val mongoSessionExpiration: Duration = servicesConfig.getDuration("mongodb.session.expiration")
  val authorisedStrideGroup: String = servicesConfig.getString("authorisedStrideGroup")
  val defaultQueryTimeRangeInMonths: Int = servicesConfig.getInt("defaultQueryTimeRangeInMonths")
  val gtmId: String = servicesConfig.getString("google-tag-manager.id")

}
