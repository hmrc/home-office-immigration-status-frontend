/*
 * Copyright 2024 HM Revenue & Customs
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

package support

import crypto.FormModelEncrypter
import models.{FormQueryModel, NinoSearchFormModel}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.Helpers.LOCATION
import repositories.SessionCacheRepository
import mocks.MockSessionCookie

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

trait ISpec extends BaseISpec with MockSessionCookie {
  val baseUrl: String = s"http://localhost:$port/check-immigration-status"

  lazy val wsClient: WSClient                     = app.injector.instanceOf[WSClient]
  lazy val cacheRepo: SessionCacheRepository      = inject[SessionCacheRepository]
  lazy val formModelEncrypter: FormModelEncrypter = inject[FormModelEncrypter]

  def setFormQuery(formModel: NinoSearchFormModel, sessionId: String): Unit = {
    val encryptedFormModel =
      formModelEncrypter.encryptSearchFormModel(formModel, sessionId, "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8=")
    val formQueryModel = FormQueryModel(sessionId, encryptedFormModel)
    Await.result(cacheRepo.set(formQueryModel).map(_ => ()), 5.seconds)
  }

  def request(path: String, sessionId: String = "123"): WSRequest =
    wsClient
      .url(s"$baseUrl$path")
      .withHttpHeaders(
        "X-Session-ID" -> sessionId,
        "Csrf-Token"   -> "nocheck"
      )

  def requestWithSession(path: String, sessionId: String): WSRequest =
    request(path, sessionId)
      .withCookies(mockSessionCookie(sessionId))
      .withFollowRedirects(false)

  def extractHeaderLocation(wsResponse: WSResponse): Option[String] =
    wsResponse.headers.get(LOCATION).map(_.head)

}
