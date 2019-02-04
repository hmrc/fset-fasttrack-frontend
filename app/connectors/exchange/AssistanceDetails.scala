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

package connectors.exchange

import forms.AssistanceDetailsForm
import play.api.libs.json.Json

import scala.language.implicitConversions

final case class AssistanceDetails(
                                    hasDisability: String,
                                    hasDisabilityDescription: Option[String],
                                    guaranteedInterview: Option[Boolean],
                                    needsSupportForOnlineAssessment: Boolean,
                                    needsSupportForOnlineAssessmentDescription: Option[String],
                                    needsSupportAtVenue: Boolean,
                                    needsSupportAtVenueDescription: Option[String]
                                  )

object AssistanceDetails {
  implicit val assistanceDetailsFormat = Json.format[AssistanceDetails]

  implicit def fromFormData(formData: AssistanceDetailsForm.Data): AssistanceDetails = {
    def toOptBoolean(optString: Option[String]) = optString match {
      case Some("Yes") => Some(true)
      case Some("No") => Some(false)
      case _ => None
    }

    AssistanceDetails(
      hasDisability = formData.hasDisability,
      hasDisabilityDescription = formData.hasDisabilityDescription,
      guaranteedInterview = toOptBoolean(formData.guaranteedInterview),
      needsSupportForOnlineAssessment = formData.needsSupportForOnlineAssessment.contains("Yes"),
      needsSupportForOnlineAssessmentDescription = formData.needsSupportForOnlineAssessmentDescription,
      needsSupportAtVenue = if (formData.needsSupportAtVenue == "Yes") true else false,
      needsSupportAtVenueDescription = formData.needsSupportAtVenueDescription
    )
  }
}
