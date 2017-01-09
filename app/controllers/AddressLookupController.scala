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
import connectors.addresslookup.AddressLookupClient
import play.api.mvc.{ Action, AnyContent }
import security.Roles.SchemesRole

trait AddressLookupController extends BaseController {
  val addressLookupClient: AddressLookupClient

  def addressLookup(postcode: String): Action[AnyContent] = CSRSecureAction(SchemesRole) {
    implicit request => implicit cachedData =>
    val decoded = java.net.URLDecoder.decode(postcode, "UTF8")
    addressLookupClient.findByPostcode(decoded, None).map(r => Ok(r.toString))
  }
}

object AddressLookupController extends AddressLookupController {
  val addressLookupClient = AddressLookupClient
  val http = CSRHttp
  val cacheClient = CSRCache
}
