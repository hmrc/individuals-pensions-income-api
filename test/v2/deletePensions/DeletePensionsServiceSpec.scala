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

package v2.deletePensions

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.deletePensions.model.request.{Def1_DeletePensionsRequestData, DeletePensionsRequestData}
import v2.models.RuleOutsideAmendmentWindowError

import scala.concurrent.Future

class DeletePensionsServiceSpec extends ServiceSpec {

  trait Test extends MockDeletePensionsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    private val nino    = Nino("AA112233A")
    private val taxYear = TaxYear.fromMtd("2019-20")

    val request: DeletePensionsRequestData = Def1_DeletePensionsRequestData(
      nino = nino,
      taxYear = taxYear
    )

    val service: DeletePensionsService = new DeletePensionsService(
      connector = mockDeletePensionsIncomeConnector
    )

  }

  "DeletePensionsIncomeService" should {
    "deletePensionsIncome" must {
      "return correct result for a success" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockDeletePensionsIncomeConnector
          .delete(request)
          .returns(Future.successful(outcome))

        await(service.deletePensions(request)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockDeletePensionsIncomeConnector
              .delete(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.deletePensions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
