/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.UUID

import connectors.exchange.{ AssessmentCentre, AssessmentScores }
import controllers.routes
import models.ApplicationData.ApplicationStatus
import models.ApplicationData.ApplicationStatus.{ CREATED, _ }
import models._
import org.scalatest.MustMatchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Lang
import play.api.test.FakeRequest
import play.api.test.Helpers._
import security.Roles.{ AssessmentCentreFailedToAttendRole, CsrAuthorization, WithdrawComponent }

class RolesSpec extends PlaySpec with MustMatchers with TableDrivenPropertyChecks {
  import RolesSpec._

  val request = FakeRequest(GET, "")

  "Withdraw Component" must {
    "be enable only for specific roles" in {
      val disabledStatuses = List(WITHDRAWN, REGISTERED, ONLINE_TEST_FAILED, ONLINE_TEST_FAILED_NOTIFIED,
        ASSESSMENT_CENTRE_FAILED, ASSESSMENT_CENTRE_FAILED_NOTIFIED)
      val enabledStatuses = ApplicationStatus.values.toList.diff(disabledStatuses)

      assertValidAndInvalidStatuses(WithdrawComponent, enabledStatuses, disabledStatuses)
    }
  }

  "Assessment Centre Failed to attend role" must {
    "be authorised only for specific roles" in {
      val enabledStatuses = List(FAILED_TO_ATTEND)
      val disabledStatuses = ApplicationStatus.values.toList.diff(enabledStatuses)

      assertValidAndInvalidStatuses(AssessmentCentreFailedToAttendRole, enabledStatuses, disabledStatuses)
    }
  }

  "The latest journey step" must {
    "be identified from the cached user" in {
        val progressToStep = Table( "candidate progress" -> "route to call",
          ProgressExamples.InitialProgress -> routes.FastTrackApplication.generalDetails(None),
          ProgressExamples.PersonalDetailsProgress -> routes.SchemeController.schemes,
          ProgressExamples.SchemePreferencesProgress -> routes.AssistanceDetailsController.present,
          ProgressExamples.AssistanceDetailsProgress -> routes.QuestionnaireController.presentStartOrContinue,
          ProgressExamples.StartedDiversityQuestionnaireProgress -> routes.QuestionnaireController.presentStartOrContinue,
          ProgressExamples.DiversityQuestionnaireProgress -> routes.QuestionnaireController.presentStartOrContinue,
          ProgressExamples.ParentalOcuppationQuestionnaireProgress -> routes.ReviewApplicationController.present(),
          ProgressExamples.ReviewProgress -> routes.SubmitApplicationController.present()
      )
        val uid = UniqueIdentifier(UUID.randomUUID)
        val applicationData =  ApplicationData(uid, uid, ApplicationStatus.IN_PROGRESS,
          progress = ProgressExamples.InitialProgress
        )
        val inProgressUser = CachedDataWithApp(
          user = CachedUser(uid, "fname", "lname", None, "email", isActive = true, lockStatus = "none"),
          application = applicationData
        )

        forAll (progressToStep) { (progress, routeToCall) =>
          val user = inProgressUser.copy(application = applicationData.copy(progress = progress))
          val result = Roles.getLatestJourneyStep(user)(request, Lang("en-GB"))

          result mustBe routeToCall
        }
    }
  }

  def assertValidAndInvalidStatuses(
    role: CsrAuthorization,
    valid: List[ApplicationStatus.Value], invalid: List[ApplicationStatus.Value]
  ) = {
    valid.foreach { validStatus =>
      withClue(s"$validStatus is not accepted by $role") {
        role.isAuthorized(activeUser(validStatus))(request) must be(true)
      }
    }

    invalid.foreach { invalidStatus =>
      withClue(s"$invalidStatus is accepted by $role") {
        role.isAuthorized(activeUser(invalidStatus))(request) must be(false)
      }
    }
  }
}

object RolesSpec {
  val id = UniqueIdentifier(UUID.randomUUID().toString)

  def activeUser(applicationStatus: ApplicationStatus) = CachedData(CachedUser(
    id,
    "John", "Biggs", None, "aaa@bbb.com", isActive = true, "locked"
  ), Some(ApplicationData(id, id, applicationStatus,
    Progress(true, true, true, true, true, true, true, true, true, true, true,
      OnlineTestProgress(true, true, true, true, true, true, true, true, true, true, true),
      true, AssessmentScores(true, true), AssessmentCentre(true, true)))))

  def registeredUser(applicationStatus: ApplicationStatus) = CachedData(CachedUser(
    id,
    "John", "Biggs", None, "aaa@bbb.com", isActive = true, "locked"
  ), None)

}
