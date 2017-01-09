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

import _root_.forms.SchemeLocationPreferenceForm.{form => preferenceForm}
import _root_.forms.{SchemeLocationPreferenceForm, SchemePreferenceForm}
import config.{AppConfig, CSRCache, CSRHttp, FrontendAppConfig}
import connectors.ApplicationClient
import play.api.libs.json.Json
import security.Roles.SchemesRole
import viewmodels.application.scheme.{SchemeLocationsViewModel, SchemePreferenceViewModel}

import scala.concurrent.Future

object SchemeController extends SchemeController {
  val http = CSRHttp
  val config = FrontendAppConfig
  val cacheClient = CSRCache
  val applicationClient = ApplicationClient
}

trait SchemeController extends BaseController {

  val config: AppConfig
  val applicationClient: ApplicationClient

  val schemeLocationForm = SchemeLocationPreferenceForm.form
  val schemeForm = SchemePreferenceForm.form

  def schemeLocations = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      applicationClient.findPersonalDetails(cachedData.user.userID, cachedData.application.applicationId).map { personalDetails =>
        val viewModel = SchemeLocationsViewModel(config.applicationSchemesFeatureConfig.preferredLocationPostCodeLookup,
          personalDetails.aLevel, personalDetails.stemLevel)

        Ok(views.html.application.scheme.wherecouldyouwork(schemeLocationForm, viewModel))
      }
  }

  def schemes = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      applicationClient.getSchemesAvailable(cachedData.application.applicationId).map { availableSchemes =>
        val viewModel = SchemePreferenceViewModel(availableSchemes.distinct)
        Ok(views.html.application.scheme.chooseyourschemes(schemeForm, viewModel))
      }
  }


  def submitLocations = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      // TODO: Process form
      schemeLocationForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest)
        },
        locationsForm => {
          // TODO: Validate locations chosen should be able to be chosen by this user (alevels/stem etc.)
          applicationClient.saveLocationChoices(cachedData.application.applicationId, locationsForm.locationIds).flatMap { _ =>
            updateProgress()(_ => Redirect(routes.SchemeController.schemes))
          }
        }
      )
  }

  def submitSchemes = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      // TODO: Process form
      schemeForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(Json.toJson(formWithErrors.errors.map(_.toString).toList)))
        },
        schemeForm => {
          // TODO: Validate schemes chosen should be able to be chosen by this user (alevels/stem etc.)
          applicationClient.saveSchemeChoices(cachedData.application.applicationId, schemeForm.schemeNames).flatMap { _ =>
            updateProgress()(_ => Redirect(routes.AssistanceDetailsController.present))
          }
        }
      )
  }
}
