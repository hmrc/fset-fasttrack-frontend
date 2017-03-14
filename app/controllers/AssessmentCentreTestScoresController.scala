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
import helpers.NotificationType._
import play.api.Logger
import security.Roles.DisplayAssessmentCentreTestScoresAndFeedbackRole
import security.SilhouetteComponent
import uk.gov.hmrc.play.http.NotFoundException
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object AssessmentCentreTestScoresController extends AssessmentCentreTestScoresController {
  val http = CSRHttp
  val cacheClient = CSRCache
  val applicationClient = ApplicationClient
  lazy val silhouette = SilhouetteComponent.silhouette
}

trait AssessmentCentreTestScoresController extends BaseController{
  val applicationClient: ApplicationClient

  def feedback = CSRSecureAppAction(DisplayAssessmentCentreTestScoresAndFeedbackRole) { implicit request =>
    implicit user =>
      applicationClient.getCandidateScores(user.application.applicationId).map { applicationScores =>
        applicationScores.scoresAndFeedback.map { scoresAndFeedback =>
          Ok(views.html.application.assessmentCentreTestScoresAndFeedback.apply(scoresAndFeedback))
        }.getOrElse {
          Logger.warn(s"Assessment centre test scores and feedback not found for user: ${user.user.userID}")
          Redirect(routes.HomeController.present()).flashing(warning("error.assessmentcentre.testfeedback.notAvailable"))
        }
      } recover {
        case _: NotFoundException =>
          Logger.warn(s"Assessment centre test scores and feedback not found for user: ${user.user.userID}")
          Redirect(routes.HomeController.present()).flashing(warning("error.assessmentcentre.testfeedback.notAvailable"))
      }
  }
}
