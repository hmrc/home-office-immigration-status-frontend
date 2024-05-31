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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.{OK, UNAUTHORIZED}
import support.WireMockSupport

trait AuthStubs {
  me: WireMockSupport =>

  def givenRequestIsNotAuthorised(mdtpDetail: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(UNAUTHORIZED)
            .withHeader("WWW-Authenticate", s"""MDTP detail="$mdtpDetail"""")
        )
    )

  def givenAuthorisedForStride(strideGroup: String, strideUserId: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .withRequestBody(
          equalToJson(
            s"""
               |{
               |  "authorise": [
               |    {
               |      "identifiers": [],
               |      "state": "Activated",
               |      "enrolment": "$strideGroup"
               |    },
               |    {
               |      "authProviders": [
               |        "PrivilegedApplication"
               |      ]
               |    }
               |  ],
               |  "retrieve": ["optionalCredentials","allEnrolments"]
               |}
           """.stripMargin,
            true,
            true
          )
        )
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(s"""
                 |{
                 |  "optionalCredentials":{
                 |    "providerId": "$strideUserId",
                 |    "providerType": "PrivilegedApplication"
                 |  },
                 |  "allEnrolments":[
                 |    {"key":"$strideGroup"}
                 |  ]
                 |}
       """.stripMargin)
        )
    )

  def verifyAuthoriseAttempt(): Unit =
    verify(1, postRequestedFor(urlEqualTo("/auth/authorise")))

}
