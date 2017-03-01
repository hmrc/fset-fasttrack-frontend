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

import _root_.forms.SignInForm
import com.mohiva.play.silhouette.api._
import config.{ CSRCache, CSRHttp }
import security._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object SimpleController extends SimpleController {
  val http = CSRHttp
  val cacheClient = CSRCache
  lazy val silhouette: Silhouette[SecurityEnvironment] = SilhouetteComponent.silhouette
}

trait SimpleController extends BaseController {

  val present = CSRUserAwareAction { implicit request =>
      implicit user =>
        request.identity match {
          case None =>
            Future.successful(Ok(views.html.index.simplewelcome(SignInForm.form)))
          case Some(u) =>
            Future.successful(Ok("You're already logged in!"))
        }
  }

  val signIn = CSRUserAwareAction { implicit request =>
    implicit user =>
      SignInForm.form.bindFromRequest.fold(
        invalidForm =>
          Future.successful(Ok(views.html.index.simplewelcome(invalidForm))),
        data => Future.successful(Ok("Logged in?"))
      )
  }
}
