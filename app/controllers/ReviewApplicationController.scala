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

import com.mohiva.play.silhouette.api.Silhouette
import config.{ CSRCache, CSRHttp }
import connectors.ApplicationClient._
import connectors.ApplicationClient
import helpers.NotificationType._
import play.api.{ Logger, Play }
import security.Roles.ReviewRole
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import security.{ SecurityEnvironment, SilhouetteComponent }

object ReviewApplicationController extends ReviewApplicationController(ApplicationClient, CSRCache) {
  override val http: CSRHttp = ApplicationClient.http
  val cacheClient = CSRCache
  lazy val silhouette = SilhouetteComponent.silhouette
}

abstract class ReviewApplicationController(applicationClient: ApplicationClient, cacheClient: CSRCache)
  extends BaseController {

  def present = CSRSecureAppAction(ReviewRole) { implicit request =>
    implicit user =>
      (for {
        personalDetails <- applicationClient.getPersonalDetails(user.user.userID, user.application.applicationId)
        assistanceDetails <- applicationClient.getAssistanceDetails(user.user.userID, user.application.applicationId)
        schemeLocationChoices <- applicationClient.getSchemeLocationChoices(user.application.applicationId)
        schemeChoices <- applicationClient.getSchemeChoices(user.application.applicationId)
      } yield {
        Ok(views.html.application.review(personalDetails, assistanceDetails, schemeLocationChoices, schemeChoices, user.application))
      }).recover {
        case ex @ (_: PersonalDetailsNotFound | _: AssistanceDetailsNotFound | _: SchemeLocationChoicesNotFound | _: SchemeChoicesNotFound) =>
          Logger.warn("Preview section reached prematurely with exception", ex)
          Redirect(routes.HomeController.present()).flashing(warning("info.cannot.review.yet"))
      }
  }

  def submit = CSRSecureAppAction(ReviewRole) { implicit request =>
    implicit user =>
      applicationClient.updateReview(user.application.applicationId).flatMap { _ =>
        updateProgress() { usr =>
          Redirect(routes.SubmitApplicationController.present())
        }
      }
  }

//  def submit = CSRSecureAppAction(ReviewRole) { implicit request =>
//    implicit user =>
//      applicationClient.updateReview(user.application.applicationId).flatMap { _ =>
//        updateProgress() { u =>
//          if (StartQuestionnaireRole.isAuthorized(u) || QuestionnaireInProgressRole.isAuthorized(u)) {
//            Redirect(routes.QuestionnaireController.start())
//          } else {
//            Redirect(routes.SubmitApplicationController.present())
//          }
//        }
//      }
//  }
}
