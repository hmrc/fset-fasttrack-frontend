/*
 * Copyright 2019 HM Revenue & Customs
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

package forms

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{ Form, FormError }
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.language.implicitConversions

object SchemeLocationPreferenceForm {

  val form = Form(
    mapping(
      "locationIds" -> of(schemeLocationsFormatter("locationIds"))
    )(Data.apply)(Data.unapply)
  )

  def schemeLocationsFormatter(formKey: String) = new Formatter[List[String]] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], List[String]] = {
      getLocationSchemesByPriority(key, data) match {
        case selectedLocations if selectedLocations.isEmpty => Left(List(FormError(formKey, Messages("location.required"))))
        case selectedLocations => Right(selectedLocations)
      }
    }

    def unbind(key: String, value: List[String]): Map[String, String] = {
      value.zipWithIndex.collect {
        case (scheme, index) => s"$key[$index]" -> scheme
      }.toMap
    }
  }

  def getLocationSchemesByPriority(key: String, data: Map[String, String]) = {
    val validLocationSchemesParams = (name: String, value: String) => name.startsWith(s"${key}_") && value.nonEmpty
    val priority: String => Int = _.split("_").last.toInt
    data.filter(pair => validLocationSchemesParams(pair._1, pair._2))
      .collect { case (name, value) => priority(name) -> value }
      .toList
      .sortBy {
        _._1
      }
      .map {
        _._2
      }
      .distinct
  }

  case class Data(
    locationIds: List[String]
  )
}
