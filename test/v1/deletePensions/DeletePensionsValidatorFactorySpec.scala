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

package v1.deletePensions

import shared.utils.UnitSpec
import shared.config.MockAppConfig
import v1.deletePensions.def1.Def1_DeletePensionsValidator

class DeletePensionsValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private val invalidNino    = "not-a-nino"
  private val invalidTaxYear = "not-a-tax-year"

  val validatorFactory = new DeletePensionsValidatorFactory

  "validator" should {
    "return the Def1 validator" when {

      "given any valid request" in {
        val result = validatorFactory.validator(validNino, validTaxYear)
        result shouldBe a[Def1_DeletePensionsValidator]
      }

      "given any invalid request" in {
        val result = validatorFactory.validator(invalidNino, invalidTaxYear)
        result shouldBe a[Def1_DeletePensionsValidator]
      }
    }

  }

}
