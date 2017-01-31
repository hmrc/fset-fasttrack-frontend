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
import models.ApplicationData.ApplicationStatus
import models.UniqueIdentifier
import play.api.mvc.{ Action, AnyContent }
import security.Roles.{ DisplayOnlineTestSectionRole, OnlineTestInvitedRole }

import scala.concurrent.Future

object OnlineTestController extends OnlineTestController {
  val http = CSRHttp
  val cacheClient = CSRCache
}

trait OnlineTestController extends BaseController {

  // TODO: I think the names can be improved
  // startTests is plural but then we call getTestAssesment.
  // It also seems we are using different names to refer to the same thing: "Tests", "onlineTest", "TestAssesment"
  // I suggest rename startTests to start as we are in OnlineTestController
  // getTestAssesment returns an Future[OnlineTestDetails], I think a more appropriate name would be
  // getOnlineTestDetails or rename OnlineTestDetails to TestAssesment.
  // then ".flatMap { onlineTest =>" should be consistent which whatever choice
  // To sum up it could be like this:
  /*
  def start = CSRSecureAppAction(OnlineTestInvitedRole) { implicit request =>
    implicit user =>
      getOnlineTestDetails(user.user.userID).flatMap { onlineTestDetails =>
        updateStatusOnlineTestDetails(user.user.userID, ApplicationStatus.ONLINE_TEST_STARTED).map { _ =>
          Redirect(onlineTestDetails.getTestLink())
        }
      }
  }

  def complete(token: UniqueIdentifier) = CSRUserAwareAction { implicit request =>
    implicit user =>
      completeOnlineTest(token).map { _ =>
        Ok(views.html.application.onlineTestSuccess())
      }
  }
*/

  def startOrContinueTest(cubiksUserId: Int): Action[AnyContent] = CSRSecureAppAction(OnlineTestInvitedRole) { implicit request =>
    implicit user =>
      getTestAssessment(user.user.userID).flatMap { onlineTest =>

        val maybeUpdateTestProfile = if (!onlineTest.isStarted) {
          startOnlineTests(cubiksUserId)
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
      getPDFReport(user.application.applicationId).map { pdfBinary =>
        Ok(pdfBinary).as("application/pdf")
          .withHeaders(("Content-Disposition", s"""attachment;filename="report-${user.application.applicationId}.pdf" """))
      }
  }

  def complete(token: UniqueIdentifier): Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      completeTests(token).map { _ =>
        Ok(views.html.application.onlineTestSuccess())
      }
  }

}
