/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers.test

import config.CSRHttp
import connectors.addresslookup.AddressLookupClient
import controllers.BaseController
import play.api.mvc.Action

trait AddressLookupController extends BaseController with AddressLookupClient {
  // TODO Add permissions into this once the feature is ready to be moved from test routes
  def addressLookup(postcode: String) = Action.async { implicit request =>
    val decoded = java.net.URLDecoder.decode(postcode, "UTF8")
    findByPostcode(decoded, None).map(r => Ok(r.toString))
  }
}

object AddressLookupController extends AddressLookupController {
  val http = CSRHttp
  val addressLookupEndpoint = config.FrontendAppConfig.addressLookupConfig.url
}
