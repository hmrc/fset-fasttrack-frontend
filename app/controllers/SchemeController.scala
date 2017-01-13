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

import _root_.forms.{ SchemeLocationPreferenceForm, SchemePreferenceForm }
import config.CSRHttp
import _root_.forms.SchemeLocationPreferenceForm.{form => preferenceForm}
import _root_.forms.{SchemeLocationPreferenceForm, SchemePreferenceForm}
import config.{AppConfig, CSRCache, CSRHttp, FrontendAppConfig}
import connectors.ApplicationClient
import connectors.ApplicationClient.{ LocationPreferencesNotFound, SchemePreferencesNotFound }
import models.CachedDataWithApp
import play.api.data.Form
import play.api.mvc.Request
import security.Roles.SchemesRole
import viewmodels.application.scheme.{SchemeLocationsViewModel, SchemePreferenceViewModel}

object SchemeController extends SchemeController {
  val http = CSRHttp
  val config = FrontendAppConfig
  val cacheClient = CSRCache
  val applicationClient = ApplicationClient
}

trait SchemeController extends BaseController {

  val applicationClient: ApplicationClient

  val schemeLocationForm = SchemeLocationPreferenceForm.form
  val schemeForm = SchemePreferenceForm.form

  def schemeLocations = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      applicationClient.getSchemeLocationChoices(cachedData.application.applicationId).flatMap {
        locations => displaySchemeLocations(
          schemeLocationForm.fill(SchemeLocationPreferenceForm.Data(locations.map(_.id))))
      }.recoverWith {
        case _: LocationPreferencesNotFound => displaySchemeLocations(schemeLocationForm)
      }
  }

  def schemes = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      applicationClient.getSchemeChoices(cachedData.application.applicationId).flatMap {
        schemes => displaySchemes(
          schemeForm.fill(SchemePreferenceForm.Data(schemes.map(_.id), orderAgreed = true)))
      }.recoverWith {
        case _: SchemePreferencesNotFound => displaySchemes(schemeForm)
      }
  }


  def submitLocations = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      schemeLocationForm.bindFromRequest.fold(
        displaySchemeLocations,
        locationsForm => {
          applicationClient.saveLocationChoices(cachedData.application.applicationId, locationsForm.locationIds).flatMap { _ =>
            updateProgress()(_ => Redirect(routes.SchemeController.schemes()))
          }
        }
      )
  }

  def submitSchemes = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      schemeForm.bindFromRequest.fold(
        displaySchemes,
        schemeForm => {
          applicationClient.saveSchemeChoices(cachedData.application.applicationId, schemeForm.schemes).flatMap { _ =>
            updateProgress()(_ => Redirect(routes.AssistanceController.present()))
          // TODO: Validate schemes chosen should be able to be chosen by this user (alevels/stem etc.)
          applicationClient.saveSchemeChoices(cachedData.application.applicationId, schemeForm.schemeNames).flatMap { _ =>
            updateProgress()(_ => Redirect(routes.AssistanceDetailsController.present))
          }
        }
      )
  }

  private def displaySchemeLocations(form: Form[SchemeLocationPreferenceForm.Data])
                            (implicit request: Request[_], cachedData: CachedDataWithApp) = {
    for {
      personalDetails <- applicationClient.findPersonalDetails(cachedData.user.userID, cachedData.application.applicationId)
      schemeLocations <- applicationClient.getSchemesAndLocationsByEligibility(personalDetails.aLevel,
        personalDetails.stemLevel, None, None)
    } yield {
      val viewModel = SchemeLocationsViewModel(personalDetails.aLevel, personalDetails.stemLevel)
      Ok(views.html.application.scheme.wherecouldyouwork(form, viewModel, personalDetails, schemeLocations))
    }
  }

  private def displaySchemes(form: Form[SchemePreferenceForm.Data])
                                    (implicit request: Request[_], cachedData: CachedDataWithApp) = {
    applicationClient.getSchemesAvailable(cachedData.application.applicationId).map { availableSchemes =>
      val viewModel = SchemePreferenceViewModel(availableSchemes.distinct)
      Ok(views.html.application.scheme.chooseyourschemes(form, viewModel))
    }
  }
}
