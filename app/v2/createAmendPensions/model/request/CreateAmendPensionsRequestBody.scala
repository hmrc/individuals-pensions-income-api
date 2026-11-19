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

package v2.createAmendPensions.model.request

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, OWrites, Reads}

case class CreateAmendPensionsRequestBody(foreignPensions: Option[Seq[CreateAmendForeignPensionsItem]],
                                          overseasPensionContributions: Option[Seq[CreateAmendOverseasPensionContributions]])

object CreateAmendPensionsRequestBody {

  implicit val reads: Reads[CreateAmendPensionsRequestBody] = (
    (JsPath \ "foreignPensions").readNullable[Seq[CreateAmendForeignPensionsItem]] and
      (JsPath \ "overseasPensionContributions").readNullable[Seq[CreateAmendOverseasPensionContributions]]
  )(CreateAmendPensionsRequestBody.apply)

  implicit val writes: OWrites[CreateAmendPensionsRequestBody] = (
    (JsPath \ "foreignPension").writeNullable[Seq[CreateAmendForeignPensionsItem]] and
      (JsPath \ "overseasPensionContribution").writeNullable[Seq[CreateAmendOverseasPensionContributions]]
  )(w => Tuple.fromProductTyped(w))

}
