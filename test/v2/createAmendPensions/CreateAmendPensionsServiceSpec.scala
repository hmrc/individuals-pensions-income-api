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

package v2.createAmendPensions

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.createAmendPensions.def1.model.request.{CreateAmendForeignPensionsItem, CreateAmendOverseasPensionContributions}
import v2.createAmendPensions.model.request.{Def1_CreateAmendPensionsRequestBody, Def1_CreateAmendPensionsRequestData}
import v2.models.RuleOutsideAmendmentWindowError

import scala.concurrent.Future

class CreateAmendPensionsServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"

  private val foreignPensionsModel = Seq(
    CreateAmendForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = Some(false),
      taxableAmount = 3.23
    ),
    CreateAmendForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.23),
      taxTakenOff = Some(3.21),
      specialWithholdingTax = Some(4.32),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 5.55
    )
  )

  private val overseasPensionContributionsModel = Seq(
    CreateAmendOverseasPensionContributions(
      customerReference = Some("PENSIONINCOME555"),
      exemptEmployersPensionContribs = 300.33,
      migrantMemReliefQopsRefNo = Some("QOPS000001"),
      dblTaxationRelief = Some(1.23),
      dblTaxationCountryCode = Some("ENG"),
      dblTaxationArticle = Some("AB1123-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-654321")
    ),
    CreateAmendOverseasPensionContributions(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    )
  )

  val amendPensionsRequest: Def1_CreateAmendPensionsRequestData = Def1_CreateAmendPensionsRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = Def1_CreateAmendPensionsRequestBody(
      foreignPensions = Some(foreignPensionsModel),
      overseasPensionContributions = Some(overseasPensionContributionsModel)
    )
  )

  trait Test extends MockCreateAmendPensionsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: CreateAmendPensionsService = new CreateAmendPensionsService(
      connector = mockCreateAmendPensionsConnector
    )

  }

  "AmendPensionsService" when {
    "amendPensions" must {
      "return correct result for a success" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendPensionsConnector
          .createAmendPensions(amendPensionsRequest)
          .returns(Future.successful(outcome))

        await(service.createAmendPensions(amendPensionsRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockCreateAmendPensionsConnector
              .createAmendPensions(amendPensionsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.createAmendPensions(amendPensionsRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("INVALID_CORRELATIONID", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("UNPROCESSABLE_ENTITY", InternalError),
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
