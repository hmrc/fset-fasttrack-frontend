package controllers

import config.{ CSRCache, CSRHttp }
import connectors.ApplicationClient
import connectors.exchange.ProgressResponse
import models.{ CachedDataWithApp, ProgressResponseExamples, UniqueIdentifier }
import models.services.UserCacheService
import org.mockito.Mockito.when
import security.SilhouetteComponent
import testkit.BaseControllerSpec
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class AssessmentCentreTestScoresControllerSpec extends BaseControllerSpec {

  trait TestFixture {
    val mockApplicationClient = mock[ApplicationClient]
    val mockCacheClient = mock[CSRCache]
    val mockUserService = mock[UserCacheService]
    val mockCSRHttp = mock[CSRHttp]

    abstract class TestableAssessmentCentreTestScoresController extends AssessmentCentreTestScoresController(mockApplicationClient, mockCacheClient)
      with TestableSecureActions

    def controller(progressResponse: ProgressResponse = ProgressResponseExamples.InParentalOccupationQuestionnaire)
                  (implicit candidateWithApp: CachedDataWithApp = currentCandidateWithApp) =
      new TestableAssessmentCentreTestScoresController {
        override val CandidateWithApp: CachedDataWithApp = candidateWithApp

        override val http: CSRHttp = mockCSRHttp
        override val cacheClient = mockCacheClient

        override lazy val silhouette = SilhouetteComponent.silhouette

        override val env = mockSecurityEnvironment

        when(mockSecurityEnvironment.userService).thenReturn(mockUserService)

        override def getApplicationProgress(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[ProgressResponse] = {
          Future.successful(progressResponse)
        }
      }
  }

}
