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

package config

import javax.inject.Inject

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, SecuredRequest, UnsecuredErrorHandler }
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.typesafe.config.Config
import controllers.routes
import filters.CookiePolicyFilter
import forms.{ SignInForm, SignUpForm }
import helpers.NotificationType._
import models.{ CachedData, SecurityUser }
import net.ceedubs.ficus.Ficus._
import play.api.i18n.{ I18nSupport, Lang, MessagesApi }
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result, _ }
import play.api._
import play.api.inject.guice.GuiceApplicationLoader
import play.twirl.api.Html
import security.SecurityEnvironment
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.config.{ AppName, ControllerConfig, RunMode }
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CustomSecuredErrorHandler @Inject() (val messagesApi: MessagesApi) extends SecuredErrorHandler with I18nSupport {

  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    val sec = request.asInstanceOf[SecuredRequest[SecurityEnvironment, SecurityUser]]
    val headerCarrier = HeaderCarrier.fromHeadersAndSession(sec.headers, Some(sec.session))
    sec.identity.toUserFuture(headerCarrier).map {
      case Some(user: CachedData) if user.user.isActive => Redirect(routes.HomeController.present).flashing(danger("access.denied"))
      case _ => Redirect(routes.ActivationController.present).flashing(danger("access.denied"))
    }
  }

  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] =
    Future.successful(Redirect(routes.SignInController.present))
}

abstract class DevelopmentFrontendGlobal
  extends DefaultFrontendGlobal {

  import FrontendAppConfig.feedbackUrl

  override val auditConnector = FrontendAuditConnector
  override val loggingFilter = LoggingFilter
  override val frontendAuditFilter = AuditFilter

  override def frontendFilters: Seq[EssentialFilter] = CookiePolicyFilter +: defaultFrontendFilters

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    views.html.error_template(pageTitle, heading, message)(rh, feedbackUrl)

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig("microservice.metrics")
}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object LoggingFilter extends FrontendLoggingFilter with MicroserviceFilterSupport{
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object AuditFilter extends FrontendAuditFilter with RunMode with AppName with MicroserviceFilterSupport{

  override lazy val maskedFormFields = Seq(
    SignInForm.passwordField,
    SignUpForm.passwordField,
    SignUpForm.confirmPasswordField,
    SignUpForm.fakePasswordField
  )

  override lazy val applicationPort = None

  override lazy val auditConnector = FrontendAuditConnector

  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}

object DevelopmentFrontendGlobal extends DevelopmentFrontendGlobal {
  override def onStart(app: Application) = {
    if (app.mode == Mode.Prod) Logger.warn("WHITE-LISTING DISABLED: Loading Development Frontend Global")
    super.onStart(app)
  }
}

object ProductionFrontendGlobal extends DevelopmentFrontendGlobal {
  override def filters = WhitelistFilter +: super.filters
}
