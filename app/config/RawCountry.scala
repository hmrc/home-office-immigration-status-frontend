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

import play.api.libs.json.{Format, Json}

case class RawCountries(countries: Seq[RawCountry])

object RawCountries {
  implicit val format: Format[RawCountries] = Json.format[RawCountries]
}

case class RawCountry(name: String, alpha2: String, alpha3: String)

object RawCountry {
  implicit val format: Format[RawCountry] = Json.format[RawCountry]
}
