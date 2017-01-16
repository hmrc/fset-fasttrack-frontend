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

import config.{CSRCache, CSRHttp}
import connectors.ApplicationClient._
import connectors.ApplicationClient
import helpers.NotificationType._
import play.api.Logger
import security.Roles.{QuestionnaireInProgressRole, ReviewRole, StartQuestionnaireRole}

object ReviewApplicationController extends ReviewApplicationController {
  val http = CSRHttp
  val cacheClient = CSRCache
  val applicationClient = ApplicationClient
}

trait ReviewApplicationController extends BaseController {

  val applicationClient: ApplicationClient

  def present = CSRSecureAppAction(ReviewRole) { implicit request =>
    implicit user =>
      val personalDetailsFut = findPersonalDetails(user.user.userID, user.application.applicationId)
      val assistanceDetailsFut = getAssistanceDetails(user.user.userID, user.application.applicationId)
      val locationChoicesFut = applicationClient.getSchemeLocationChoices(user.application.applicationId)
      val schemeChoicesFut = applicationClient.getSchemeChoices(user.application.applicationId)

      (for {
        gd <- personalDetailsFut
        ad <- assistanceDetailsFut
        slc <- locationChoicesFut
        sc <- schemeChoicesFut
      } yield {
        Ok(views.html.application.review(gd, ad, slc, sc, user.application))
      }).recover {
        case ex @ (_: PersonalDetailsNotFound | _: AssistanceDetailsNotFound | _: SchemePreferencesNotFound | _: LocationPreferencesNotFound) =>
          Logger.warn("Preview section reached prematurely with exception", ex)
          Redirect(routes.HomeController.present()).flashing(warning("info.cannot.review.yet"))
      }
  }

  def submit = CSRSecureAppAction(ReviewRole) { implicit request =>
    implicit user =>
      updateReview(user.application.applicationId).flatMap { _ =>
        updateProgress() { u =>
          if (StartQuestionnaireRole.isAuthorized(u) || QuestionnaireInProgressRole.isAuthorized(u)) {
//            Redirect(routes.QuestionnaireController.start())
            Redirect(routes.QuestionnaireControllerV2.start())
          } else {
            Redirect(routes.SubmitApplicationController.present())
          }
        }
      }

  }

}
