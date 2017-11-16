/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers

import config.{ CSRCache, CSRHttp }
import connectors.ApplicationClient
import connectors.exchange.ProgressResponse
import models.{ CachedDataWithApp, ProgressResponseExamples, UniqueIdentifier }
import models.services.UserCacheService
import org.mockito.Mockito.when
import security.SilhouetteComponent
import testkit.BaseControllerSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class AssessmentCentreTestScoresControllerSpec extends BaseControllerSpec {

  trait TestFixture {
    val mockApplicationClient = mock[ApplicationClient]
    val mockCacheClient = mock[CSRCache]
    val mockUserService = mock[UserCacheService]
    val mockCSRHttp = mock[CSRHttp]

    abstract class TestableAssessmentCentreTestScoresController extends
      AssessmentCentreTestScoresController with TestableSecureActions

    def controller(progressResponse: ProgressResponse = ProgressResponseExamples.InParentalOccupationQuestionnaire)
                  (implicit candidateWithApp: CachedDataWithApp = currentCandidateWithApp) =
      new AssessmentCentreTestScoresController with TestableSecureActions {
        override val applicationClient: ApplicationClient = ApplicationClient
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
