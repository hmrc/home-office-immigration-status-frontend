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

package config

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.Environment
import play.api.libs.json.Json
import com.typesafe.config.ConfigException

@Singleton
class Countries @Inject()(environment: Environment) {

  val fileName = "location-autocomplete-alpha3.json"

  val countries: Seq[CountryInput] =
    environment
      .resourceAsStream(fileName)
      .flatMap { in =>
        val locationJsValue = Json.parse(in)
        Json
          .fromJson[RawCountries](locationJsValue)
          .asOpt
          .map { c =>
            c.countries.map { country =>
              CountryInput(value = country.alpha3, label = country.name)
            }
          }
      }
      .getOrElse {
        throw new ConfigException.BadValue(fileName, "country json does not exist")
      }

}
