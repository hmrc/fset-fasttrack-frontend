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

import _root_.forms.AssistanceDetailsForm
import config.{CSRCache, CSRHttp}
import connectors.ApplicationClient
import connectors.ApplicationClient.AssistanceDetailsNotFound
import helpers.NotificationType._
import security.Roles.AssistanceDetailsRole

import scala.concurrent.Future

object AssistanceDetailsController extends AssistanceDetailsController(ApplicationClient, CSRCache) {
  override val http: CSRHttp = ApplicationClient.http
}

abstract class AssistanceDetailsController(applicationClient: ApplicationClient, cacheClient: CSRCache)
  extends BaseController {

  def present = CSRSecureAppAction(AssistanceDetailsRole) { implicit request =>
    implicit user =>

      applicationClient.getAssistanceDetails(user.user.userID, user.application.applicationId).map { ad =>
        val form = AssistanceDetailsForm.form.fill(AssistanceDetailsForm.Data(ad))
        Ok(views.html.application.assistanceDetails(form))
      }.recover {
        case e: AssistanceDetailsNotFound => Ok(views.html.application.assistanceDetails(AssistanceDetailsForm.form))
      }
  }

  def submit = CSRSecureAppAction(AssistanceDetailsRole) { implicit request =>
    implicit user =>
      AssistanceDetailsForm.form.bindFromRequest.fold(
        invalidForm =>
          Future.successful(Ok(views.html.application.assistanceDetails(invalidForm))),
        data => {
          applicationClient.updateAssistanceDetails(user.application.applicationId, user.user.userID, data.sanitizeData).flatMap { _ =>
              updateProgress()(_ => Redirect(routes.ReviewApplicationController.present()))
            }.recover {
              case e: AssistanceDetailsNotFound =>
                Redirect(routes.HomeController.present()).flashing(danger("account.error"))
            }
          }
      )
  }
}
