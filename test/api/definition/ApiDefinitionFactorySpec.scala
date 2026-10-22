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

package api.definition

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.APIStatus.{ALPHA, BETA}
import api.mocks.MockHttpClient
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId

import scala.language.reflectiveCalls

class ApiDefinitionFactorySpec extends UnitSpec with MockAppConfig {

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {

      s"return the expected status" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "BETA"

        val result: APIStatus = checkBuildApiStatus(Version9)
        result shouldBe BETA
      }

    }

    "the 'apiStatus' parameter is present but invalid" should {
      s"default to alpha" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "not-a-status"

        checkBuildApiStatus(Version9) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw an exception" in new Test {
        MockedAppConfig.apiStatus(Version9) returns "DEPRECATED"

        MockedAppConfig
          .deprecationFor(Version9)
          .returns("deprecatedOn date is required for a deprecated version".invalid)
          .anyNumberOfTimes()

        val exception: Exception = intercept[Exception] {
          checkBuildApiStatus(Version9)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }

  }

  "the access level is set" when {
    "the controlled access flag is enabled" should {
      "return CONTROLLED" in {

        MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"
        MockedAppConfig.endpointsEnabled(Version2)
        MockedAppConfig.apiStatus(Version2) returns "BETA"
        MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()

        MockedAppConfig.controlledAccessEnabled returns true

        val definition: Definition = new ApiDefinitionFactory(mockAppConfig).definition

        definition.api.versions.head.access shouldBe APIAccessType.CONTROLLED
      }
    }

    "the controlled access flag is disabled" should {
      "return PUBLIC" in {

        MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"
        MockedAppConfig.endpointsEnabled(Version2)
        MockedAppConfig.apiStatus(Version2) returns "BETA"
        MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()

        MockedAppConfig.controlledAccessEnabled returns false

        val definition: Definition = new ApiDefinitionFactory(mockAppConfig).definition

        definition.api.versions.head.access shouldBe APIAccessType.PUBLIC
      }
    }
  }

  class Test extends MockHttpClient with MockAppConfig {
    MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"

    protected val apiDefinitionFactory: ApiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig) {

      override lazy val definition: Definition = Definition(
        APIDefinition(
          "test API definition",
          "description",
          "context",
          List("category"),
          List(APIVersion(Version1, APIStatus.BETA, access = APIAccessType.PUBLIC, endpointsEnabled = true)),
          None
        )
      )

    }

    def checkBuildApiStatus(version: Version): APIStatus = apiDefinitionFactory.buildAPIStatus(version)

    protected def setupMockConfig(version: Version): Unit = {
      MockedAppConfig
        .deprecationFor(version)
        .returns(NotDeprecated.valid)
        .anyNumberOfTimes()
    }

  }

}
