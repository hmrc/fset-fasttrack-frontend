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

package forms

import connectors.exchange.AssistanceDetails
import play.api.data.{ FormError, Form }
import play.api.data.Forms.{mapping, of, optional}
import play.api.data.format.Formatter

object AssistanceDetailsForm {

  private[forms] val deptMaxLength = 256

  // scalastyle:off cyclomatic.complexity
  val disabilityAndGisDependentFormatter = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val disabilityCheck = data.get("hasDisability")
      val gisCheck = data.get("guaranteedInterview")
      val value = data.get(key).filterNot(_.trim.isEmpty) // value for needsSupportForOnlineAssessment

      // scalastyle:off
      println(s"****** disabilityAndGisDependentFormatter - disabilityCheck=$disabilityCheck, gisCheck=$gisCheck, value=$value")
      (disabilityCheck, gisCheck, value) match {
        case (Some("Yes"), Some("Yes"), None) =>
          //scalastyle:off
          println(s"****** disabilityAndGisDependentFormatter - CASE 1")
          Right(Some("No"))
        case (Some("No"), Some("No"), None) =>
          //scalastyle:off
          println(s"****** disabilityAndGisDependentFormatter - CASE 2")
          Left(List(FormError(key, s"error.$key.required")))
        case (None, None, None) =>
          println(s"****** disabilityAndGisDependentFormatter - CASE 3")
          Left(List(FormError(key, s"error.$key.required")))
        case (Some("Yes"), None, None) =>
          println(s"****** disabilityAndGisDependentFormatter - CASE 4")
          Left(List(FormError(key, s"error.$key.required")))
        case (Some("Yes"), Some("No"), None) =>
          println(s"****** disabilityAndGisDependentFormatter - CASE 5")
          Left(List(FormError(key, s"error.$key.required")))
        case (Some("Yes"), Some("No"), Some("Yes")) =>
          println(s"****** disabilityAndGisDependentFormatter - CASE 6")
          Right(Some("Yes"))
        case (Some("No"), None, Some("Yes")) =>
          println(s"****** disabilityAndGisDependentFormatter - CASE 7")
          Right(Some("Yes"))
        case (Some("No"), Some("No"), Some("Yes")) =>
          println(s"****** disabilityAndGisDependentFormatter - CASE 8")
          Right(Some("Yes"))
        case (Some("I don't know/prefer not to say"), None, Some("Yes")) =>
          println(s"****** disabilityAndGisDependentFormatter - CASE 9")
          Right(Some("Yes"))

        case _ =>
          //scalastyle:off
          println(s"****** disabilityAndGisDependentFormatter - CASE DEFAULT")
          Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }
  //scalastyle:on

  val form = Form(
    mapping(
      "hasDisability" -> Mappings.nonEmptyTrimmedText("error.hasDisability.required", 31),
      "hasDisabilityDescription" -> optional(Mappings.nonEmptyTrimmedText("error.hasDisabilityDescription.required", 2048)),
      "guaranteedInterview" -> of(requiredFormatterWithMaxLengthCheck("hasDisability", "guaranteedInterview", None)),
      "needsSupportForOnlineAssessment" -> of(disabilityAndGisDependentFormatter),
      "needsSupportForOnlineAssessmentDescription" -> of(requiredFormatterWithMaxLengthCheck("needsSupportForOnlineAssessment",
      "needsSupportForOnlineAssessmentDescription", Some(2048))),
      "needsSupportAtVenue" -> Mappings.nonEmptyTrimmedText("error.needsSupportAtVenue.required", 31),
      "needsSupportAtVenueDescription" -> of(requiredFormatterWithMaxLengthCheck("needsSupportAtVenue", "needsSupportAtVenueDescription",
        Some(2048)))
    )(Data.apply)(Data.unapply)
  )

  case class Data(
                   hasDisability: String,
                   hasDisabilityDescription: Option[String],
                   guaranteedInterview: Option[String],
                   needsSupportForOnlineAssessment: Option[String],
                   needsSupportForOnlineAssessmentDescription: Option[String],
                   needsSupportAtVenue: String,
                   needsSupportAtVenueDescription: Option[String]) {
    def sanitizeData: AssistanceDetailsForm.Data = {
      AssistanceDetailsForm.Data(
        hasDisability,
        if (hasDisability == "Yes") hasDisabilityDescription else None,
        if (hasDisability == "Yes") guaranteedInterview else None,
        if ((hasDisability == "Yes" && guaranteedInterview.contains("No")) || hasDisability != "Yes") needsSupportForOnlineAssessment else None,
        if ((hasDisability == "Yes" && guaranteedInterview.contains("No") && needsSupportForOnlineAssessment.contains("Yes"))
          || hasDisability != "Yes") { needsSupportForOnlineAssessmentDescription } else { None },
        needsSupportAtVenue,
        if (needsSupportAtVenue.contains("Yes")) needsSupportAtVenueDescription else None
      )
    }

    def needsAssistance: Boolean = hasDisability == "No" || hasDisability == "Prefer not to say"
  }

  object Data {
    def apply(ad: AssistanceDetails): Data = {
      def toOptString(optBoolean: Option[Boolean]) = optBoolean match {
        case Some(true) => Some("Yes")
        case Some(false) => Some("No")
        case _ => None
      }

      AssistanceDetailsForm.Data(
        ad.hasDisability,
        ad.hasDisabilityDescription,
        toOptString(ad.guaranteedInterview),
        if (ad.needsSupportForOnlineAssessment) Some("Yes") else Some("No"),
        ad.needsSupportForOnlineAssessmentDescription,
        if (ad.needsSupportAtVenue) "Yes" else "No",
        ad.needsSupportAtVenueDescription
      )
    }
  }
}
