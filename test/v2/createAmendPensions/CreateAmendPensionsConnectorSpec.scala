/*
 * Copyright 2025 HM Revenue & Customs
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

import shared.config.MockAppConfig
import shared.connectors.ConnectorSpec
import shared.mocks.MockHttpClient
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.createAmendPensions.def1.model.request.{CreateAmendForeignPensionsItem, CreateAmendOverseasPensionContributions}
import v2.createAmendPensions.model.request.{Def1_CreateAmendPensionsRequestBody, Def1_CreateAmendPensionsRequestData}

import scala.concurrent.Future

class CreateAmendPensionsConnectorSpec extends ConnectorSpec {

  private val nino: String    = "AA111111A"
  private val taxYear: String = "2019-20"

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

  private val amendPensionsRequestBody: Def1_CreateAmendPensionsRequestBody = Def1_CreateAmendPensionsRequestBody(
    foreignPensions = Some(foreignPensionsModel),
    overseasPensionContributions = Some(overseasPensionContributionsModel)
  )

  private def amendPensionsRequest(taxYear: String): Def1_CreateAmendPensionsRequestData = Def1_CreateAmendPensionsRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = amendPensionsRequestBody
  )

  trait Test extends MockHttpClient with MockAppConfig {

    val connector: CreateAmendPensionsConnector = new CreateAmendPensionsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnvironment returns "ifs-environment"
    MockedAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "AmendPensionsConnector" when {
    val outcome = Right(ResponseWrapper(correlationId, ()))
    "amendPensions" must {
      "return a 204 status for a success scenario" in new IfsTest with Test {

        willPut(url"$baseUrl/income-tax/income/pensions/$nino/$taxYear", amendPensionsRequestBody).returns(Future.successful(outcome))

        await(connector.createAmendPensions(amendPensionsRequest(taxYear))) shouldBe outcome
      }
    }
    "amend pensions for a TYS tax year" must {
      "return a 204 status for a success scenario" in new IfsTest with Test {

        willPut(
          url"$baseUrl/income-tax/income/pensions/23-24/$nino",
          amendPensionsRequestBody
        ).returns(Future.successful(outcome))

        await(connector.createAmendPensions(amendPensionsRequest("2023-24"))) shouldBe outcome

      }
    }
  }

}
