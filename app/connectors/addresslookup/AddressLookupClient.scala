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

package connectors.addresslookup

import java.net.URLEncoder

import config.CSRHttp
import uk.gov.hmrc.play.http.{ HeaderCarrier, _ }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * The following client has been take from taken from https://github.com/hmrc/address-reputation-store. The project has
  * not been added as a dependency, as it brings in many transitive dependencies that are not needed,
  * as well as data cleansing/ingestion and backward compatibility logic that is not needed for this project.
  * If the version 2 api gets deprecated, then these DTOs will have to change.
  * There have been some minor changes made to the code to ensure that it compiles and passes scalastyle,
  * but there is some copied code that is not idiomatic Scala and should be changed at some point in the future
  */

trait AddressLookupClient {

  def addressLookupEndpoint: String
  val http: CSRHttp

  private def url = s"$addressLookupEndpoint/v2/uk/addresses"

  def findById(id: String)(implicit hc: HeaderCarrier): Future[Option[AddressRecord]] = {
    assert(id.length <= 100, "Postcodes cannot be larger than 100 characters")
    val uq = "/" + enc(id)
    http.GET[Option[AddressRecord]](url + uq).recover {
      case _: NotFoundException => None
    }
  }

  def findByUprn(uprn: Long)(implicit hc: HeaderCarrier): Future[List[AddressRecord]] = {
    val uq = "?uprn=" + uprn.toString
    http.GET[List[AddressRecord]](url + uq)
  }

  def findByPostcode(postcode: String, filter: Option[String])(implicit hc: HeaderCarrier): Future[List[AddressRecord]] = {
    val safePostcode = postcode.replaceAll("[^A-Za-z0-9]", "")
    assert(safePostcode.length <= 10, "Postcodes cannot be larger than 10 characters")
    val pq = "?postcode=" + enc(safePostcode)
    val fq = filter.map(fi => "&filter=" + enc(fi)).getOrElse("")
    http.GET[List[AddressRecord]](url + pq + fq)
  }

  def findByOutcode(outcode: Outcode, filter: String)(implicit hc: HeaderCarrier): Future[List[AddressRecord]] = {
    val pq = "?outcode=" + outcode.toString
    val fq = "&filter=" + enc(filter)
    http.GET[List[AddressRecord]](url + pq + fq)
  }

  private def enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
