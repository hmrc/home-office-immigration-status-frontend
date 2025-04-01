/*
 * Copyright 2025 HM Revenue & Customs
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

package endpoints

import play.api.Application
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import support.ISpec
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_urlEncodedSimpleForm

import scala.concurrent.Future

class ShutteredISpec extends ISpec {

  override def fakeApplication(): Application = appBuilder.configure("isShuttered" -> true).build()

  val get: String => Future[WSResponse]  = request(_).get()
  val post: String => Future[WSResponse] = request(_).post(Map.empty[String, String])

  "an endpoint" when {
    Seq[(String, String => Future[WSResponse])](
      "/"                   -> get,
      "/search-by-nino"     -> get,
      "/search-by-nino"     -> post,
      "/search-by-passport" -> get,
      "/search-by-passport" -> post,
      "/status-result"      -> get,
      "/foo"                -> get
    ).foreach { case (path, request) =>
      s"path is $path and method is ${request.getClass.getName} and app is shuttered" should {
        "return the shutter page" in {
          val result: WSResponse = await(request(path))

          result.status shouldBe SERVICE_UNAVAILABLE
          result.body     should include(htmlEscapedMessage("shuttering.title"))
        }
      }
    }
  }

}
