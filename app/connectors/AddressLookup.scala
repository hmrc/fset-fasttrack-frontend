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

package connectors.addresslookup

import java.net.URLEncoder

import config.CSRHttp
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import java.util.regex.Pattern

/**
  * The following DTOs are taken from https://github.com/hmrc/address-reputation-store. The project has
  * not been added as a dependency, as it brings in many transitive dependencies that are not needed,
  * as well as data cleansing/ingestion and backward compatibility logic that is not needed for this project.
  * If the version 2 api gets deprecated, then these DTOs will have to change.
  * There have been some minor changes made to the code to ensure that it compiles and passes scalastyle,
  * but there is some copied code that is not idiomatic Scala and should be changed at some point in the future
  */

case class LocalCustodian(code: Int, name: String)

object LocalCustodian {
  implicit val reads = Json.reads[LocalCustodian]
}

/** Represents a country as per ISO3166. */
case class Country(
                    // ISO3166-1 or ISO3166-2 code, e.g. "GB" or "GB-ENG" (note that "GB" is the official
                    // code for UK although "UK" is a reserved synonym and may be used instead)
                    // See https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
                    // and https://en.wikipedia.org/wiki/ISO_3166-2:GB
                    code: String,
                    // The printable name for the country, e.g. "United Kingdom"
                    name: String)

object Country {
  implicit val countryReads = Json.reads[Country]
}

case class Outcode(area: String, district: String) {
  override lazy val toString: String = area + district
}

object Outcode {
  implicit val outcodeReads = Json.reads[Outcode]
}

object Countries {
  // note that "GB" is the official ISO code for UK, although "UK" is a reserved synonym and is less confusing
  val UK = Country("UK", "United Kingdom")
  val GB = Country("GB", "United Kingdom") // special case provided for in ISO-3166
  val GG = Country("GG", "Guernsey")
  val IM = Country("IM", "Isle of Man")
  val JE = Country("JE", "Jersey")

  val England = Country("GB-ENG", "England")
  val Scotland = Country("GB-SCT", "Scotland")
  val Wales = Country("GB-WLS", "Wales")
  val Cymru = Country("GB-CYM", "Cymru")
  val NorthernIreland = Country("GB-NIR", "Northern Ireland")

  private val all = List(UK, GB, GG, IM, JE, England, Scotland, Wales, Cymru, NorthernIreland)

  def find(code: String): Option[Country] = all.find(_.code == code)

  def findByName(name: String): Option[Country] = all.find(_.name == name)

  // TODO this is possibly not good enough - should consult a reference HMG-approved list of countries
}

/**
  * Address typically represents a postal address.
  * For UK addresses, 'town' will always be present.
  * For non-UK addresses, 'town' may be absent and there may be an extra line instead.
  */
case class Address(lines: List[String],
                   town: Option[String],
                   county: Option[String],
                   postcode: String,
                   subdivision: Option[Country],
                   country: Country) {

  import Address._

  def nonEmptyFields: List[String] = lines ::: town.toList ::: county.toList ::: List(postcode)

  /** Gets a conjoined representation, excluding the country. */
  def printable(separator: String): String = nonEmptyFields.mkString(separator)

  /** Gets a single-line representation, excluding the country. */
  def printable: String = printable(", ")

  def line1: String = if (lines.nonEmpty) lines.head else ""

  def line2: String = if (lines.size > 1) lines(1) else ""

  def line3: String = if (lines.size > 2) lines(2) else ""

  def line4: String = if (lines.size > 3) lines(3) else ""

  def longestLineLength: Int = nonEmptyFields.map(_.length).max

  def truncatedAddress(maxLen: Int = maxLineLength): Address =
    Address(lines.map(limit(_, maxLen)), town.map(limit(_, maxLen)), county.map(limit(_, maxLen)), postcode, subdivision, country)

}

object Address {
  val maxLineLength = 35
  val danglingLetter: Pattern = Pattern.compile(".* [A-Z0-9]$")
  implicit val addressFormat = Json.reads[Address]

  private[addresslookup] def limit(str: String, max: Int): String = {
    var s = str
    while (s.length > max && s.indexOf(", ") > 0) {
      s = s.replaceFirst(", ", ",")
    }
    if (s.length > max) {
      s = s.substring(0, max).trim
      if (Address.danglingLetter.matcher(s).matches()) {
        s = s.substring(0, s.length - 2)
      }
      s
    }
    else { s }
  }
}

case class LatLong(lat: Double, long: Double) {
  def toLocation: String = lat.toString + "," + long.toString
}

case class AddressRecord(
                          id: String,
                          uprn: Option[Long],
                          address: Address,
                          // ISO639-1 code, e.g. 'en' for English
                          // see https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
                          language: String,
                          localCustodian: Option[LocalCustodian],
                          blpuState: Option[String],
                          logicalState: Option[String],
                          streetClassification: Option[String]) {

  import Address._
  def truncatedAddress(maxLen: Int = Address.maxLineLength): AddressRecord =
    if (address.longestLineLength <= maxLen) { this }
    else { copy(address = address.truncatedAddress(maxLen)) }

  def withoutMetadata: AddressRecord = copy(blpuState = None, logicalState = None, streetClassification = None)

  def geoLocation: LatLong = ???
}

object AddressRecord {
  implicit val addressRecordFormat = Json.reads[AddressRecord]
}

trait AddressLookupClient{

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
