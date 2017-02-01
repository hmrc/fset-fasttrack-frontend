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
import connectors.OnlineTestClient
import models.ApplicationData.ApplicationStatus
import models.UniqueIdentifier
import play.api.mvc.{ Action, AnyContent }
import security.Roles.{ DisplayOnlineTestSectionRole, OnlineTestInvitedRole }

import scala.concurrent.Future

object OnlineTestController extends OnlineTestController {
  val http = CSRHttp
  val cacheClient = CSRCache

  val onlineTestClient = OnlineTestClient
}

trait OnlineTestController extends BaseController {

  def onlineTestClient: OnlineTestClient

  def startOrContinueTest(cubiksUserId: Int): Action[AnyContent] = CSRSecureAppAction(OnlineTestInvitedRole) { implicit request =>
    implicit user =>
      onlineTestClient.getTestAssessment(user.user.userID).flatMap { onlineTest =>

        val maybeUpdateTestProfile = if (!onlineTest.isStarted) {
          onlineTestClient.startOnlineTests(cubiksUserId)
        } else {
         Future.successful(())
        }

        maybeUpdateTestProfile.map { _ =>
          Redirect(onlineTest.onlineTestLink)
        }
      }
  }

  def downloadPDFReport: Action[AnyContent] = CSRSecureAppAction(DisplayOnlineTestSectionRole) { implicit request =>
    implicit user =>
      onlineTestClient.getPDFReport(user.application.applicationId).map { pdfBinary =>
        Ok(pdfBinary).as("application/pdf")
          .withHeaders(("Content-Disposition", s"""attachment;filename="report-${user.application.applicationId}.pdf" """))
      }
  }

  def completeByToken(token: UniqueIdentifier): Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      onlineTestClient.completeTests(token).map { _ =>
        Ok(views.html.application.onlineTestSuccess())
      }
  }

  def completeTest(cubiksUserId: Int): Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      onlineTestClient.completeTests(

  }

}
