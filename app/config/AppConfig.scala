/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import com.google.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig, configuration: Configuration, env: Environment) {

  val appName: String = servicesConfig.getString("appName")
  val shuttered: Boolean = servicesConfig.getBoolean("isShuttered")
  val authBaseUrl: String = servicesConfig.baseUrl("auth")
  val homeOfficeImmigrationStatusProxyBaseUrl: String = servicesConfig.baseUrl("home-office-immigration-status-proxy")
  val mongoSessionExpiration: Int = servicesConfig.getInt("mongodb.ttl.seconds")
  val authorisedStrideGroup: String = servicesConfig.getString("authorisedStrideGroup")
  val defaultQueryTimeRangeInMonths: Int = servicesConfig.getInt("defaultQueryTimeRangeInMonths")
  val gtmId: String = servicesConfig.getString("google-tag-manager.id")
  val helpdeskUrl: String = servicesConfig.getString("it.helpdesk.url")
  val httpHeaderCacheControl: String = servicesConfig.getString("httpHeaders.cacheControl")
  val mongoEncryptionKey = servicesConfig.getString("mongodb.encryption.key")

  val isDevEnv =
    if (env.mode.equals(Mode.Test)) false
    else configuration.getOptional[String]("run.mode").forall(Mode.Dev.toString.equals)

}
