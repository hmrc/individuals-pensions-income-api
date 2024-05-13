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

package v1.controllers

import api.controllers.RequestHandlerOld
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import shared.config.AppConfig
import shared.controllers.{AuditHandlerOld, AuthorisedController, EndpointLogContext, RequestContext}
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v1.controllers.requestParsers.CreateAmendPensionsRequestParser
import v1.models.request.createAmendPensions.CreateAmendPensionsRawData
import v1.services.CreateAmendPensionsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendPensionsController @Inject() (val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               parser: CreateAmendPensionsRequestParser,
                                               service: CreateAmendPensionsService,
                                               auditService: AuditService,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendPensionsController",
      endpointName = "createAmendPensions"
    )

  def createAmendPensions(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: CreateAmendPensionsRawData = CreateAmendPensionsRawData(
        nino = nino,
        taxYear = taxYear,
        body = AnyContentAsJson(request.body)
      )

      val requestHandler = RequestHandlerOld
        .withParser(parser)
        .withService(service.createAmendPensions)
        .withNoContentResult(OK)
        .withAuditing(
          AuditHandlerOld(
            auditService,
            auditType = "CreateAmendPensionsIncome",
            transactionName = "create-amend-pensions-income",
            params = Map("nino" -> nino, "taxYear" -> taxYear),
            requestBody = Some(request.body)
          )
        )

      requestHandler.handleRequest(rawData)
    }

}
