/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.ApplicationClient.{ AssistanceDetailsNotFound, PersonalDetailsNotFound, SchemeChoicesNotFound, SchemeLocationChoicesNotFound }
import connectors.exchange._
import models.SecurityUserExamples._
import models._
import models.services.UserCacheService
import org.mockito.Matchers.{ eq => eqTo, _ }
import org.mockito.Mockito.{ when, _ }
import play.api.test.Helpers._
import security.SilhouetteComponent
import testkit.BaseControllerSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class ReviewApplicationControllerSpec extends BaseControllerSpec {

  // This is the implicit user
  override def currentCandidateWithApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    CachedDataExample.InProgressInAssistanceDetailsApplication.copy(userId = ActiveCandidate.user.userID))

  "present" should {
    "load review page for existing application" in new TestFixture {
      val result = controller.present()(fakeRequest)
      status(result) must be(OK)
      val content = contentAsString(result)
      content must include("<title>Check your application")
      content must include(s"""<span class="your-name" id="bannerUserName">${currentCandidate.user.preferredName.get}</span>""")
      content must include("""<section class="section-border" id="choiceInfo">""")
      content must include("Will you need extra support for your online tests?")
    }

    "redirect to home page with error when personal details cannot be found" in new TestFixture {
      when(mockApplicationClient.getPersonalDetails(eqTo(currentUserId), eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new PersonalDetailsNotFound))
      val result = controller.present()(fakeRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.HomeController.present().url))
    }

    "redirect to home page with error when scheme choices cannot be found" in new TestFixture {
      when(mockApplicationClient.getSchemeChoices(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new SchemeChoicesNotFound))
      val result = controller.present()(fakeRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.HomeController.present().url))
    }

    "redirect to home page with error when scheme location choices cannot be found" in new TestFixture {
      when(mockApplicationClient.getSchemeLocationChoices(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new SchemeLocationChoicesNotFound))
      val result = controller.present()(fakeRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.HomeController.present().url))
    }

    "redirect to home page with error when assistance details cannot be found" in new TestFixture {
      when(mockApplicationClient.getAssistanceDetails(eqTo(currentUserId), eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new AssistanceDetailsNotFound))
      val result = controller.present()(fakeRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.HomeController.present().url))
    }
  }

  "submit review" should {
    "redirect to questionnaire start page" in new TestFixture {
      val Request = fakeRequest
      when(mockApplicationClient.updateReview(eqTo(currentApplicationId))(any[HeaderCarrier])).thenReturn(Future.successful(()))
      when(mockApplicationClient.getApplicationProgress(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(ProgressResponseExamples.InReview))

      val Application = currentCandidateWithApp.application.copy(progress = ProgressResponseExamples.InParentalOccupationQuestionnaire)
      val UpdatedCandidate = currentCandidate.copy(application = Some(Application))
      when(mockUserService.save(eqTo(UpdatedCandidate))(any[HeaderCarrier])).thenReturn(Future.successful(UpdatedCandidate))

      val result = controller.submit()(Request)

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.SubmitApplicationController.present().url))
    }
  }

  trait TestFixture {
    val mockApplicationClient = mock[ApplicationClient]
    val mockCacheClient = mock[CSRCache]
    val mockUserService = mock[UserCacheService]
    val mockCSRHttp = mock[CSRHttp]

    when(mockApplicationClient.getPersonalDetails(eqTo(currentUserId), eqTo(currentApplicationId))(any[HeaderCarrier]))
      .thenReturn(Future.successful(PersonalDetailsExamples.FullDetails))
    when(mockApplicationClient.getSchemeChoices(eqTo(currentApplicationId))(any[HeaderCarrier]))
      .thenReturn(Future.successful(List(SchemeInfoExamples.ALevelsScheme)))
    when(mockApplicationClient.getSchemeLocationChoices(eqTo(currentApplicationId))(any[HeaderCarrier]))
      .thenReturn(Future.successful(List(LocationSchemesExamples.LocationSchemes1, LocationSchemesExamples.LocationSchemes2)))
    when(mockApplicationClient.getAssistanceDetails(eqTo(currentUserId), eqTo(currentApplicationId))(any[HeaderCarrier]))
      .thenReturn(Future.successful(AssistanceDetailsExamples.DisabilityGisAndAdjustments))

    class TestablePreviewApplicationController extends ReviewApplicationController(mockApplicationClient, mockCacheClient)
      with TestableSecureActions {
      val http: CSRHttp = mockCSRHttp
      val cacheClient = mockCacheClient

      override lazy val silhouette = SilhouetteComponent.silhouette

      override val env = mockSecurityEnvironment

      when(mockSecurityEnvironment.userService).thenReturn(mockUserService)

      override def getApplicationProgress(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[ProgressResponse] = {
        Future.successful(ProgressResponseExamples.InParentalOccupationQuestionnaire)
      }
    }

    def controller(implicit candidateWithApp: CachedDataWithApp = currentCandidateWithApp) = new TestablePreviewApplicationController {
      override val CandidateWithApp: CachedDataWithApp = candidateWithApp
    }
  }

}
