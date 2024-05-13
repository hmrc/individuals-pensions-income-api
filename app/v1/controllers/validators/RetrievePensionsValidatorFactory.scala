package v1.controllers.validators

import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import shared.config.AppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v1.models.request.retrievePensions.RetrievePensionsRequestData

import javax.inject.Inject

class RetrievePensionsValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minimumTaxYear = appConfig.minimumPermittedTaxYear
  private lazy val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromDownstreamInt(minimumTaxYear))

  def validator(nino: String, taxYear: String): Validator[RetrievePensionsRequestData] =
    new Validator[RetrievePensionsRequestData] {

      def validate: Validated[Seq[MtdError], RetrievePensionsRequestData] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear)
        ).mapN(RetrievePensionsRequestData)

    }

}
