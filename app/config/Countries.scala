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

import com.google.inject.{Inject, Singleton}
import play.api.Environment
import play.api.libs.json.{Json, Reads}
import com.typesafe.config.ConfigException

@Singleton
class Countries @Inject()(environment: Environment) {

  private def alpha2ToHmrcName: Map[String, String] =
    environment
      .resourceAsStream("location-autocomplete-canonical-list.json")
      .flatMap { inputStream =>
        val locationJsValue = Json.parse(inputStream)
        Json
          .fromJson[Array[(String, String)]](locationJsValue)
          .asOpt
          .map {
            _.map {
              case (name, code) =>
                val alpha2 = ":([A-Z]{2})$".r.findFirstMatchIn(code).map(_.group(1))
                alpha2 -> name
            }.collect { case (Some(code), name) => code -> name }.toMap
          }
      }
      .getOrElse(throw new ConfigException.BadValue(
        "location-autocomplete-canonical-list.json",
        "Alpha2 to Name map cannot be constructed."))

  case class ISOCountry(name: String, alpha2: String, alpha3: String, numeric: String)
  object ISOCountry {
    implicit val reads: Reads[ISOCountry] = Json.reads[ISOCountry]
  }

  private def iso3166CountryCodes: Seq[ISOCountry] =
    environment
      .resourceAsStream("ISO_3166-alpha3-alpha2-numeric.json")
      .flatMap { in =>
        val locationJsValue = Json.parse(in)
        Json
          .fromJson[Seq[ISOCountry]](locationJsValue)
          .asOpt
      }
      .getOrElse {
        throw new ConfigException.BadValue("ISO_3166-alpha3-alpha2-numeric.json", "ISO codes json does not exist")
      }

  val countries: Seq[CountryInput] = iso3166CountryCodes.map { isoCountry =>
    val hmrcName = alpha2ToHmrcName.getOrElse(isoCountry.alpha2, isoCountry.name)

    CountryInput(isoCountry.alpha3, hmrcName)
  }

}
