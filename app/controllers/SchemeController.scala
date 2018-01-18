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

import _root_.forms.{ SchemeLocationPreferenceForm, SchemePreferenceForm }
import com.mohiva.play.silhouette.api.Silhouette
import config.{ CSRCache, CSRHttp, FrontendAppConfig }
import connectors.ApplicationClient
import connectors.ApplicationClient.{ SchemeChoicesNotFound, SchemeLocationChoicesNotFound }
import connectors.exchange.SchemeInfo
import models.CachedDataWithApp
import play.api.Play
import play.api.data.Form
import play.api.mvc.Request
import security.Roles.SchemesRole
import viewmodels.application.scheme.{ SchemeLocationsViewModel, SchemePreferenceViewModel }
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import security.{ SecurityEnvironment, SilhouetteComponent }

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object SchemeController extends SchemeController {
  val http = CSRHttp
  val config = FrontendAppConfig
  val cacheClient = CSRCache
  val applicationClient = ApplicationClient
  lazy val silhouette = SilhouetteComponent.silhouette
}

trait SchemeController extends BaseController {

  val applicationClient: ApplicationClient

  val schemeLocationForm = SchemeLocationPreferenceForm.form
  val schemeForm = SchemePreferenceForm.form

  def schemes = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      applicationClient.getSchemeChoices(cachedData.application.applicationId).flatMap {
        schemes => displaySchemes(
          schemeForm.fill(SchemePreferenceForm.Data(schemes.map(_.id), orderAgreed = true, eligible = true)))
      }.recoverWith {
        case _: SchemeChoicesNotFound => displaySchemes(schemeForm)
      }
  }

  def schemeLocations = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      applicationClient.getSchemeLocationChoices(cachedData.application.applicationId).flatMap {
        locations => displaySchemeLocations(
          schemeLocationForm.fill(SchemeLocationPreferenceForm.Data(locations.map(_.id))))
      }.recoverWith {
        case _: SchemeLocationChoicesNotFound => displaySchemeLocations(schemeLocationForm)
      }
  }

  def submitSchemes = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      schemeForm.bindFromRequest.fold(
        displaySchemes,
        schemeForm => {
          for {
            _ <- removePreviousSchemeLocationChoicesMayBe(schemeForm.schemes)
            _ <- applicationClient.saveSchemeChoices(cachedData.application.applicationId, schemeForm.schemes)
            redirect <- updateProgress()(_ => Redirect(routes.SchemeController.schemeLocations()))
          } yield {
            redirect
          }
        }
      )
  }

  def submitLocations = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      schemeLocationForm.bindFromRequest.fold(
        displaySchemeLocations,
        locationsForm => {
          applicationClient.saveLocationChoices(cachedData.application.applicationId, locationsForm.locationIds).flatMap { _ =>
            updateProgress()(_ => Redirect(routes.AssistanceDetailsController.present()))
          }
        }
      )
  }

  private def displaySchemeLocations(form: Form[SchemeLocationPreferenceForm.Data])
                                    (implicit request: Request[_], cachedData: CachedDataWithApp) = {
    for {
      personalDetails <- applicationClient.getPersonalDetails(cachedData.user.userID, cachedData.application.applicationId)
      schemeLocations <- applicationClient.getEligibleSchemeLocations(cachedData.application.applicationId, None, None)
    } yield {
      val viewModel = SchemeLocationsViewModel(personalDetails.aLevel, personalDetails.stemLevel)
      Ok(views.html.application.scheme.chooseyourlocations(form, viewModel, personalDetails, schemeLocations))
    }
  }

  private def displaySchemes(form: Form[SchemePreferenceForm.Data])
                                    (implicit request: Request[_], cachedData: CachedDataWithApp) = {
    applicationClient.getEligibleSchemes(cachedData.application.applicationId).map { availableSchemes =>
      val viewModel = SchemePreferenceViewModel(availableSchemes.distinct)
      Ok(views.html.application.scheme.chooseyourschemes(form, viewModel))
    }
  }

  private def removePreviousSchemeLocationChoicesMayBe(newSchemeChoices: List[String])(
    implicit user: CachedDataWithApp, hc: HeaderCarrier) = {
    def schemeChoicesChanged(previousSchemeChoices: List[SchemeInfo]) = {
      newSchemeChoices.toSet != previousSchemeChoices.map(_.id).toSet
    }

    def removeSchemeChoicesMayBe(previousSchemeChoices: List[SchemeInfo]) = schemeChoicesChanged(previousSchemeChoices) match {
      case true => removeSchemeLocations(user.application.applicationId)
      case false => Future.successful(())
    }

    (for {
      previousSchemeChoices <- applicationClient.getSchemeChoices(user.application.applicationId)
      _ <- removeSchemeChoicesMayBe(previousSchemeChoices)
    } yield {

    }) recover {
      case e: SchemeChoicesNotFound => ()
    }
  }
}
