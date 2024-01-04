package shared.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.NO_CONTENT
import support.WireMockMethods

object AuditStub extends WireMockMethods {

  private val auditUri: String = s"/write/audit.*"

  def audit(): StubMapping = {
    when(method = POST, uri = auditUri)
      .thenReturn(status = NO_CONTENT)
  }

}
