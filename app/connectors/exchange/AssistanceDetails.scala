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

package connectors.exchange

import play.api.libs.json.Json

final case class AssistanceDetails(
                                  // TODO: old, they will be removed
                                    needsAssistance: String,
                                    typeOfdisability: Option[List[String]],
                                    detailsOfdisability: Option[String],
                                    guaranteedInterview: Option[String],
                                    needsAdjustment: Option[String],
                                    typeOfAdjustments: Option[List[String]],
                                    otherAdjustments: Option[String],
                                    campaignReferrer: Option[String],
                                    campaignOther: Option[String],
                                  // TODO: new
                                    hasDisability: String,
                                    hasDisabilityDescription: Option[String],
                                    guaranteedInterviewBoolean: Option[Boolean], // TODO: Change this
                                    needsSupportForOnlineAssessment: Option[Boolean],
                                    needsSupportForOnlineAssessmentDescription: Option[String],
                                    needsSupportAtVenue: Option[Boolean],
                                    needsSupportAtVenueDescription: Option[String],
                                    // TODO: Change adjustments
                                    confirmedAdjustments: Option[Boolean],
                                    numericalTimeAdjustmentPercentage: Option[Int],
                                    verbalTimeAdjustmentPercentage: Option[Int]
) {
}

object AssistanceDetails {
  implicit val assistanceDetailsFormat = Json.format[AssistanceDetails]
}
