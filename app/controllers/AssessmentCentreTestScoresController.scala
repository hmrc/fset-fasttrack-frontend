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

package controllers

import config.{ CSRCache, CSRHttp }
import connectors.ApplicationClient
import helpers.NotificationType._
import models.UniqueIdentifier
import play.api.Logger
import security.Roles.DisplayAssessmentCentreTestScoresAndFeedbackRole
import security.SilhouetteComponent
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.http.NotFoundException

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
      def notFoundHandler(userId: UniqueIdentifier) = {
        Logger.warn(s"Assessment centre test scores and feedback not found for user: $userId")
        Redirect(routes.HomeController.present()).flashing(warning("error.assessmentcentre.testfeedback.notAvailable"))
      }

      (for {
        scoresAndFeedbackOpt <- applicationClient.getCandidateScores(user.application.applicationId)
        competencyAverageResult <- applicationClient.getAssessmentCentreCompetencyAverageResult(user.application.applicationId)
      } yield {
        scoresAndFeedbackOpt.map { scoresAndFeedback =>
          Ok(views.html.application.assessmentCentreTestScoresAndFeedback.apply(scoresAndFeedback, competencyAverageResult))
        }.getOrElse(notFoundHandler(user.user.userID))
      }).recover {
        case _: NotFoundException => notFoundHandler(user.user.userID)
      }
  }
}
