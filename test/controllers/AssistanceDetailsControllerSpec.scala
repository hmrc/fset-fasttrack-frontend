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
import connectors.ApplicationClient.AssistanceDetailsNotFound
import models.SecurityUserExamples._
import models._
import org.mockito.Matchers.{ eq => eqTo, _ }
import org.mockito.Mockito._
import play.api.test.Helpers._
import testkit.BaseControllerSpec
import uk.gov.hmrc.play.http.HeaderCarrier
import connectors.exchange.{ AssistanceDetailsExamples, ProgressResponse }
import _root_.forms.AssistanceDetailsFormExamples
import com.mohiva.play.silhouette.api.{ LoginInfo, Silhouette }
import com.mohiva.play.silhouette.impl.User
import models.services.UserCacheService
import com.mohiva.play.silhouette.test._
import security.{ SecurityEnvironment, SilhouetteComponent }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AssistanceDetailsControllerSpec extends BaseControllerSpec {

  // This is the implicit user
  override def currentCandidateWithApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    CachedDataExample.InProgressInSchemePreferencesApplication.copy(userId = ActiveCandidate.user.userID))

  "present" should {
    "load assistance details page for the new user" in new TestFixture {
      when(mockApplicationClient.getAssistanceDetails(eqTo(currentUserId), eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new AssistanceDetailsNotFound))

      val result = controller().present()(fakeRequest)
      status(result) must be(OK)
      val content = contentAsString(result)
      content must include("<title>Disability and health conditions")
      content must include("Will you need extra support for your online tests?")
      content must include(s"""<span class="your-name" id="bannerUserName">${currentCandidate.user.preferredName.get}</span>""")
    }

    "load assistance details page for the already created assistance details" in new TestFixture {
      when(mockApplicationClient.getAssistanceDetails(eqTo(currentUserId), eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(AssistanceDetailsExamples.DisabilityGisAndAdjustments))

      val result = controller().present()(fakeRequest)

      status(result) must be(OK)
      val content = contentAsString(result)
      content must include("<title>Disability and health conditions")
      content must include("Will you need extra support for your online tests?")
      content must include(s"""<span class="your-name" id="bannerUserName">${currentCandidate.user.preferredName.get}</span>""")
      content must include("Some adjustment")
    }
  }

  "submit assistance details" should {
    // TODO IS: fix this
    "update assistance details and redirect to questionnaire start page if it has not been started" ignore new TestFixture {
      val actualCurrentCandidateWithApp = CachedDataWithApp(ActiveCandidate.user,
        CachedDataExample.InProgressInSchemePreferencesApplication.copy(userId = ActiveCandidate.user.userID))

      val Request = fakeRequest.withFormUrlEncodedBody(AssistanceDetailsFormExamples.DisabilityGisAndAdjustmentsFormUrlEncodedBody: _*)
      when(mockApplicationClient.updateAssistanceDetails(eqTo(currentApplicationId), eqTo(currentUserId),
        eqTo(AssistanceDetailsExamples.DisabilityGisAndAdjustments))(any[HeaderCarrier])).thenReturn(Future.successful(()))

      val Application = actualCurrentCandidateWithApp.application.copy(progress = ProgressResponseExamples.InAssistanceDetails)
      val UpdatedCandidate = currentCandidate.copy(application = Some(Application))
      when(mockUserService.save(eqTo(UpdatedCandidate))(any[HeaderCarrier])).thenReturn(Future.successful(UpdatedCandidate))

      val result = controller(ProgressResponseExamples.InAssistanceDetails)(actualCurrentCandidateWithApp).submit()(Request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.QuestionnaireController.presentStartOrContinue().url))
    }

    "update assistance details and redirect to questionnaire if questionnaire is started and diversity page " +
      "completed but neither education nor parental occupation are completed" ignore new TestFixture {
      val actualCurrentCandidateWithApp = CachedDataWithApp(ActiveCandidate.user,
        CachedDataExample.InProgressInDiversityQuestionnaireApplication.copy(userId = ActiveCandidate.user.userID))

      val Request = fakeRequest.withFormUrlEncodedBody(AssistanceDetailsFormExamples.DisabilityGisAndAdjustmentsFormUrlEncodedBody: _*)
      when(mockApplicationClient.updateAssistanceDetails(eqTo(currentApplicationId), eqTo(currentUserId),
        eqTo(AssistanceDetailsExamples.DisabilityGisAndAdjustments))(any[HeaderCarrier])).thenReturn(Future.successful(()))

      val Application = actualCurrentCandidateWithApp.application.copy(progress = ProgressResponseExamples.InDiversityQuestionnaire)
      val UpdatedCandidate = currentCandidate.copy(application = Some(Application))
      when(mockUserService.save(eqTo(UpdatedCandidate))(any[HeaderCarrier])).thenReturn(Future.successful(UpdatedCandidate))

      val result = controller(ProgressResponseExamples.InDiversityQuestionnaire)(actualCurrentCandidateWithApp).submit()(Request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.QuestionnaireController.submitContinue().url))
    }

    "update assistance details and redirect to review if questionnaire is completed" ignore new TestFixture {
      val actualCurrentCandidateWithApp = CachedDataWithApp(ActiveCandidate.user,
        CachedDataExample.InProgressInParentalOccupationQuestionnaireApplication.copy(userId = ActiveCandidate.user.userID))

      val Request = fakeRequest.withFormUrlEncodedBody(AssistanceDetailsFormExamples.DisabilityGisAndAdjustmentsFormUrlEncodedBody: _*)
      when(mockApplicationClient.updateAssistanceDetails(eqTo(currentApplicationId), eqTo(currentUserId),
        eqTo(AssistanceDetailsExamples.DisabilityGisAndAdjustments))(any[HeaderCarrier])).thenReturn(Future.successful(()))

      val Application = actualCurrentCandidateWithApp.application.copy(progress = ProgressResponseExamples.InParentalOccupationQuestionnaire)
      val UpdatedCandidate = currentCandidate.copy(application = Some(Application))
      when(mockUserService.save(eqTo(UpdatedCandidate))(any[HeaderCarrier])).thenReturn(Future.successful(UpdatedCandidate))

      val result = controller(ProgressResponseExamples.InParentalOccupationQuestionnaire)(actualCurrentCandidateWithApp).submit()(Request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ReviewApplicationController.present().url))
    }
  }

  trait TestFixture {
    val mockApplicationClient = mock[ApplicationClient]
    val mockCacheClient = mock[CSRCache]
    val mockUserService = mock[UserCacheService]
    val mockCSRHttp = mock[CSRHttp]

    abstract class TestableAssistanceDetailsController extends AssistanceDetailsController(mockApplicationClient, mockCacheClient)
      with TestableSecureActions

    def controller(progressResponse: ProgressResponse = ProgressResponseExamples.InParentalOccupationQuestionnaire)
                  (implicit candidateWithApp: CachedDataWithApp = currentCandidateWithApp) =
      new TestableAssistanceDetailsController {
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
