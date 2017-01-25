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

package security

import controllers.routes
import models.ApplicationData.ApplicationStatus._
import models.{ CachedData, CachedDataWithApp, Progress }
import play.api.i18n.Lang
import play.api.mvc.{ Call, RequestHeader }
import uk.gov.hmrc.play.http.HeaderCarrier

object Roles {

  import RoleUtils._

  trait CsrAuthorization {
    def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang): Boolean

    def isAuthorized(user: CachedDataWithApp)(implicit request: RequestHeader, lang: Lang): Boolean =
      isAuthorized(CachedData(user.user, Some(user.application)))
  }

  trait AuthorisedUser extends CsrAuthorization {
    def isEnabled(user: CachedData)(implicit request: RequestHeader, lang: Lang): Boolean

    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && isEnabled(user)
  }

  //all the roles

  object NoRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) = true
  }

  object ActivationRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      !user.user.isActive
  }

  object ActiveUserRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      user.user.isActive
  }

  object ApplicationStartRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      user.user.isActive && user.application.isEmpty
  }

  object PersonalDetailsRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(CREATED, IN_PROGRESS)
  }

  object SchemesRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasPersonalDetails(user)
  }

  object AssistanceDetailsRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasSchemesAndLocations(user)
  }

  object StartQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) &&
        !(hasDiversityQuestionnaire(user) && hasEducationQuestionnaire(user) && hasParentalOccupationQuestionnaire(user))
  }

  object QuestionnaireInProgressRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) &&
        ((hasDiversityQuestionnaire(user) || hasEducationQuestionnaire(user) || hasParentalOccupationQuestionnaire(user)) &&
          !(hasDiversityQuestionnaire(user) && hasEducationQuestionnaire(user) && hasParentalOccupationQuestionnaire(user)))
  }

  object DiversityQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasStartedQuestionnaire(user) && !hasDiversityQuestionnaire(user)
  }

  object EducationQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasDiversityQuestionnaire(user) && !hasEducationQuestionnaire(user)
  }

  object ParentalOccupationQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasEducationQuestionnaire(user) && !hasParentalOccupationQuestionnaire(user)
  }

  object QuestionnaireFinishedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasParentalOccupationQuestionnaire(user)
  }

  object ReviewRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && !statusIn(user)(CREATED) && hasParentalOccupationQuestionnaire(user)
  }

  object SubmitApplicationRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasReview(user)
  }

  object InProgressRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS)
  }

  object WithdrawApplicationRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && !statusIn(user)(IN_PROGRESS, WITHDRAWN, CREATED)
  }

  object WithdrawnApplicationRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(WITHDRAWN)
  }

  object OnlineTestInvitedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(ONLINE_TEST_INVITED, ONLINE_TEST_STARTED)
  }

  object DisplayOnlineTestSectionRole extends CsrAuthorization {
    // format: OFF
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(ONLINE_TEST_INVITED,
        ONLINE_TEST_STARTED, ONLINE_TEST_COMPLETED, ONLINE_TEST_EXPIRED,
        ALLOCATION_CONFIRMED, ALLOCATION_UNCONFIRMED, AWAITING_ALLOCATION,
        ONLINE_TEST_FAILED, ONLINE_TEST_FAILED_NOTIFIED, AWAITING_ONLINE_TEST_RE_EVALUATION, FAILED_TO_ATTEND,
        ASSESSMENT_SCORES_ENTERED, ASSESSMENT_SCORES_ACCEPTED, AWAITING_ASSESSMENT_CENTRE_RE_EVALUATION, ASSESSMENT_CENTRE_PASSED,
        ASSESSMENT_CENTRE_FAILED, ASSESSMENT_CENTRE_PASSED_NOTIFIED, ASSESSMENT_CENTRE_FAILED_NOTIFIED)
    // format: ON
  }

  object ConfirmedAllocatedCandidateRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(ALLOCATION_CONFIRMED, ASSESSMENT_SCORES_ACCEPTED,
        ASSESSMENT_SCORES_ENTERED, AWAITING_ASSESSMENT_CENTRE_RE_EVALUATION)
  }

  object UnconfirmedAllocatedCandidateRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(ALLOCATION_UNCONFIRMED)
  }

  object AssessmentCentreFailedNotifiedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(ASSESSMENT_CENTRE_FAILED_NOTIFIED)
  }

  object AssessmentCentrePassedNotifiedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      activeUserWithApp(user) && statusIn(user)(ASSESSMENT_CENTRE_PASSED_NOTIFIED)
  }

  object AssessmentCentreFailedToAttendRole extends AuthorisedUser {
    override def isEnabled(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      statusIn(user)(FAILED_TO_ATTEND)
  }

  object WithdrawComponent extends AuthorisedUser {
    override def isEnabled(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
      !statusIn(user)(IN_PROGRESS, WITHDRAWN, CREATED, ONLINE_TEST_FAILED, ONLINE_TEST_FAILED_NOTIFIED,
        ASSESSMENT_CENTRE_FAILED, ASSESSMENT_CENTRE_FAILED_NOTIFIED)
  }

  val userJourneySequence: List[(CsrAuthorization, Call)] = List(
    ApplicationStartRole -> routes.HomeController.present,
    PersonalDetailsRole -> routes.FastTrackApplication.generalDetails(None),
    SchemesRole -> routes.SchemeController.schemes(),
    AssistanceDetailsRole -> routes.AssistanceDetailsController.present,
    StartQuestionnaireRole -> routes.QuestionnaireController.start,
    DiversityQuestionnaireRole -> routes.QuestionnaireController.presentFirstPage,
    EducationQuestionnaireRole -> routes.QuestionnaireController.presentSecondPage,
    ParentalOccupationQuestionnaireRole -> routes.QuestionnaireController.presentThirdPage,
    ReviewRole -> routes.ReviewApplicationController.present,
    SubmitApplicationRole -> routes.SubmitApplicationController.present,
    DisplayOnlineTestSectionRole -> routes.HomeController.present,
    ConfirmedAllocatedCandidateRole -> routes.HomeController.present,
    UnconfirmedAllocatedCandidateRole -> routes.HomeController.present,
    WithdrawApplicationRole -> routes.HomeController.present
  ).reverse
}

object RoleUtils {
  implicit def hc(implicit request: RequestHeader): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

  def activeUserWithApp(user: CachedData)(implicit request: RequestHeader, lang: Lang) =
    user.user.isActive && user.application.isDefined

  def statusIn(user: CachedData)(status: ApplicationStatus*)(implicit request: RequestHeader, lang: Lang) =
    user.application.isDefined && status.contains(user.application.get.applicationStatus)

  def progress(implicit user: CachedData): Progress = user.application.get.progress

  def hasPersonalDetails(implicit user: CachedData) = progress.personalDetails

  def hasSchemesAndLocations(implicit user: CachedData) = progress.hasSchemeLocations && progress.hasSchemes

  def hasAssistanceDetails(implicit user: CachedData) = user.application.isDefined && progress.assistanceDetails

  def hasStartedQuestionnaire(implicit user: CachedData) = progress.startedQuestionnaire

  def hasDiversityQuestionnaire(implicit user: CachedData) = progress.diversityQuestionnaire

  def hasEducationQuestionnaire(implicit user: CachedData) = progress.educationQuestionnaire

  def hasParentalOccupationQuestionnaire(implicit user: CachedData) = progress.occupationQuestionnaire

  def hasReview(implicit user: CachedData) = progress.review
}
