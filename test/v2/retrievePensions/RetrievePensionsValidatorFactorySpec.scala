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

package v2.retrievePensions

import shared.config.MockAppConfig
import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v2.retrievePensions.def1.Def1_RetrievePensionsValidator
import v2.retrievePensions.model.request.RetrievePensionsRequestData

class RetrievePensionsValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"
  val validatorFactory     = new RetrievePensionsValidatorFactory

  "validator()" when {

    "given any request with a valid tax year" should {
      "return the Validator for schema definition 1" in {

        val result: Validator[RetrievePensionsRequestData] =
          validatorFactory.validator(validNino, validTaxYear)

        result shouldBe a[Def1_RetrievePensionsValidator]
      }
    }

  }

}
