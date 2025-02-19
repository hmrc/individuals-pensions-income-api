/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.createAmendPensions.def1.model.request

import play.api.libs.json.{JsError, JsObject, Json}
import shared.utils.UnitSpec

class CreateAmendForeignPensionsItemSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "countryCode": "DEU",
      |   "amountBeforeTax": 100.23,
      |   "taxTakenOff": 1.23,
      |   "specialWithholdingTax": 2.23,
      |   "foreignTaxCreditRelief": false,
      |   "taxableAmount": 3.23
      |}
    """.stripMargin
  )

  private val model = CreateAmendForeignPensionsItem(
    countryCode = "DEU",
    amountBeforeTax = Some(100.23),
    taxTakenOff = Some(1.23),
    specialWithholdingTax = Some(2.23),
    foreignTaxCreditRelief = Some(false),
    taxableAmount = 3.23
  )

  "AmendForeignPensionsItem" when {
    "read from valid JSON" should {
      "produce the expected AmendForeignPensionsItem object" in {
        json.as[CreateAmendForeignPensionsItem] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[CreateAmendForeignPensionsItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
