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

import config.{CSRCache, CSRHttp}
import connectors.ApplicationClient
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import security.Roles.SchemesRole

trait LocationSchemeController extends BaseController {
  val applicationClient: ApplicationClient

  def getEligibleSchemeLocations(latitudeOpt: Option[Double],
                                 longitudeOpt: Option[Double]): Action[AnyContent] = CSRSecureAppAction(SchemesRole) {
    implicit request => implicit cachedData =>
    applicationClient.getEligibleSchemeLocations(cachedData.application.applicationId, latitudeOpt, longitudeOpt).map { resp =>
      Ok(Json.toJson(resp))
    }
  }
}

object LocationSchemeController extends LocationSchemeController {
  val http = CSRHttp
  val applicationClient = ApplicationClient
  override val cacheClient: CSRCache = CSRCache
}
