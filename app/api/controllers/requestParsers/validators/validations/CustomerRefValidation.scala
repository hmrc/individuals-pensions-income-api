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

package api.controllers.requestParsers.validators.validations

import shared.models.errors.MtdError
import v1.models.CustomerRefFormatError

object CustomerRefValidation {

  private val regex = "^[0-9a-zA-Z{À-˿'}\\- _&`():.'^]{1,90}$"

  def validateOptional(customerRef: Option[String], path: Option[String] = None): List[MtdError] = customerRef match {
    case None        => NoValidationErrors
    case Some(value) => validate(value, path)
  }

  def validate(customerRef: String, path: Option[String] = None): List[MtdError] = {
    if (customerRef.matches(regex)) NoValidationErrors else List(CustomerRefFormatError.copy(paths = path.map(List(_))))
  }

}
