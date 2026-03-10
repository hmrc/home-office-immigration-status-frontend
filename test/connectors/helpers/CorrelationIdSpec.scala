/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors.helpers

import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import org.scalatestplus.play.PlaySpec
import org.scalatest.matchers.must.Matchers

class CorrelationIdSpec extends PlaySpec with Matchers {

  trait CorrelationIdTestSetup {
    val uuid = "123f4567-g89c-42c3-b456-557742330000"
    val correlationId: CorrelationId =
      new CorrelationId {
        override def generateNewUUID: String = uuid
      }
  }

  "requestID is present in the headerCarrier" must {
    "return new ID pre-appending the requestID when the requestID matches the format(8-4-4-4)" in new CorrelationIdTestSetup {
      val requestId  = "dcba0000-ij12-df34-jk56"
      val uuidLength = 24
      correlationId.id(HeaderCarrier(requestId = Some(RequestId(requestId)))) mustBe
        s"$requestId-${uuid.substring(uuidLength)}"
    }

    "return new ID when the requestID does not match the format(8-4-4-4)" in new CorrelationIdTestSetup {
      val requestId = "1a2b-ij12-df34-jk56"
      correlationId.id(HeaderCarrier(requestId = Some(RequestId(requestId)))) mustBe uuid
    }
  }

  "requestID is not present in the headerCarrier must return a new ID" must {
    "return the uuid" in new CorrelationIdTestSetup {
      correlationId.id(HeaderCarrier()) mustBe uuid
    }
  }

  "generateNewUUID" must {
    "return a valid UUID string" in {
      val correlationId = new CorrelationId

      val uuidStr = correlationId.generateNewUUID

      uuidStr must not be empty
    }
  }
}
