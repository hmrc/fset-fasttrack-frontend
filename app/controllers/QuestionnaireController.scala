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
import _root_.forms._
import connectors.exchange.Questionnaire
import models.CachedDataWithApp
import play.api.mvc.{ Result, Request }
import security.Roles.{ DiversityQuestionnaireRole, EducationQuestionnaireRole, ParentalOccupationQuestionnaireRole, StartQuestionnaireRole }
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.language.reflectiveCalls

object QuestionnaireController extends QuestionnaireController {
  val http = CSRHttp
  val cacheClient = CSRCache
}

trait QuestionnaireController extends BaseController with ApplicationClient {

  def start = CSRSecureAppAction(StartQuestionnaireRole) { implicit request =>
    implicit user =>
      val p = user.application.progress
      Future.successful {
        if (!p.diversityQuestionnaire && !p.educationQuestionnaire && !p.occupationQuestionnaire) {
          Ok(views.html.questionnaire.intro(DiversityQuestionnaireForm.acceptanceForm))
        } else {
          Ok(views.html.questionnaire.continue())
        }
      }
  }

  def submitStart = CSRSecureAppAction(StartQuestionnaireRole) { implicit request =>
    implicit user =>
      DiversityQuestionnaireForm.acceptanceForm.bindFromRequest.fold(
        errorForm => {
          Future.successful(Ok(views.html.questionnaire.intro(errorForm)))
        },
        _ => {
          val empty = Questionnaire(List())
          submitQuestionnaire(empty, "start_questionnaire")(Redirect(routes.QuestionnaireController.presentFirstPage()))
        }
      )
  }

  def submitContinue = CSRSecureAppAction(StartQuestionnaireRole) { implicit request =>
    implicit user =>
      val p = user.application.progress
      Future.successful((p.diversityQuestionnaire, p.educationQuestionnaire, p.occupationQuestionnaire) match {
        case (_, _, true) => Redirect(routes.SubmitApplicationController.present())
        case (_, true, _) => Redirect(routes.QuestionnaireController.presentThirdPage())
        case (true, _, _) => Redirect(routes.QuestionnaireController.presentSecondPage())
        case (_, _, _) => Redirect(routes.QuestionnaireController.presentFirstPage())
      })
  }

  def presentFirstPage = CSRSecureAppAction(DiversityQuestionnaireRole) { implicit request =>
    implicit user =>
      Future.successful(Ok(views.html.questionnaire.firstpage(DiversityQuestionnaireForm.form)))
  }

  def submitFirstPage = CSRSecureAppAction(DiversityQuestionnaireRole) { implicit request =>
    implicit user =>
      DiversityQuestionnaireForm.form.bindFromRequest.fold(
        errorForm => {
          Future.successful(Ok(views.html.questionnaire.firstpage(errorForm)))
        },
        data => {
          submitQuestionnaire(data.exchange, "diversity_questions_completed")(
            Redirect(routes.QuestionnaireController.presentSecondPage()))
        }
      )
  }

  def presentSecondPage = CSRSecureAppAction(EducationQuestionnaireRole) { implicit request =>
    implicit user =>
      Future.successful(Ok(views.html.questionnaire.secondpage(EducationQuestionnaireForm.form)))
  }

  def submitSecondPage = CSRSecureAppAction(EducationQuestionnaireRole) { implicit request =>
    implicit user =>
      EducationQuestionnaireForm.form.bindFromRequest.fold(
        errorForm => {
          Future.successful(Ok(views.html.questionnaire.secondpage(errorForm)))
        },
        data => {
          submitQuestionnaire(data.exchange, "education_questions_completed")(Redirect(routes.QuestionnaireController.presentThirdPage()))
        }
      )
  }

  def presentThirdPage = CSRSecureAppAction(ParentalOccupationQuestionnaireRole) { implicit request =>
    implicit user =>
      Future.successful(Ok(views.html.questionnaire.thirdpage(ParentalOccupationQuestionnaireForm.form)))
  }

  def submitThirdPage = CSRSecureAppAction(ParentalOccupationQuestionnaireRole) { implicit request =>
    implicit user =>
      ParentalOccupationQuestionnaireForm.form.bindFromRequest.fold(
        errorForm => {
          Future.successful(Ok(views.html.questionnaire.thirdpage(errorForm)))
        },
        data => {
          submitQuestionnaire(data.exchange, "occupation_questions_completed")(Redirect(routes.ReviewApplicationController.present()))
        }
      )
  }

  private def submitQuestionnaire(data: Questionnaire, sectionId: String)(onSuccess: Result)
                                 (implicit user: CachedDataWithApp, hc: HeaderCarrier, request: Request[_]) = {
    updateQuestionnaire(user.application.applicationId, sectionId, data).flatMap { _ =>
      updateProgress()(_ => onSuccess)
    }
  }
}
