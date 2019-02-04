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

object AssistanceDetailsFormExamples {
  val Yes = "Yes"
  val No = "No"

  val NoDisabilitiesForm = AssistanceDetailsForm.Data(
    hasDisability = No,
    hasDisabilityDescription = None,
    guaranteedInterview = None,
    needsSupportForOnlineAssessment = Some(No),
    needsSupportForOnlineAssessmentDescription = None,
    needsSupportAtVenue = No,
    needsSupportAtVenueDescription = None
  )

  val NoAdjustmentsForm = AssistanceDetailsForm.Data(
    hasDisability = Yes,
    hasDisabilityDescription = Some("Some disabilities"),
    guaranteedInterview = Some(No),
    needsSupportForOnlineAssessment = Some(No),
    needsSupportForOnlineAssessmentDescription = None,
    needsSupportAtVenue = No,
    needsSupportAtVenueDescription = None
  )

  val FullForm = AssistanceDetailsForm.Data(
    hasDisability = Yes,
    hasDisabilityDescription = Some("Some disabilities"),
    guaranteedInterview = Some(Yes),
    needsSupportForOnlineAssessment = Some(Yes),
    needsSupportForOnlineAssessmentDescription = Some("Some adjustments online"),
    needsSupportAtVenue = Yes,
    needsSupportAtVenueDescription = Some("Some adjustments at venue")
  )

  val Scenario1Map = Map[String, String](
    "hasDisability" -> Yes,
    "hasDisabilityDescription" -> "Some disabilities",
    "guaranteedInterview" -> Yes,
    // needsSupportForOnlineAssessment missing
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> Yes,
    "needsSupportAtVenueDescription" -> "Some adjustments at venue"
  )

  val Scenario2Map = Map[String, String](
    "hasDisability" -> No,
    "hasDisabilityDescription" -> "",
    "guaranteedInterview" -> No,
    // needsSupportForOnlineAssessment missing
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> "",
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario3Map = Map[String, String](
    // hasDisability missing
    "hasDisabilityDescription" -> "",
    // guaranteedInterview missing
    // needsSupportForOnlineAssessment missing
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> "",
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario4Map = Map[String, String](
    "hasDisability" -> Yes,
    "hasDisabilityDescription" -> "",
    // guaranteedInterview missing
    // needsSupportForOnlineAssessment missing
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> "",
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario5Map = Map[String, String](
    "hasDisability" -> Yes,
    "hasDisabilityDescription" -> "",
    "guaranteedInterview" -> No,
    // needsSupportForOnlineAssessment missing
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> "",
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario6Map = Map[String, String](
    "hasDisability" -> Yes,
    "hasDisabilityDescription" -> "",
    "guaranteedInterview" -> No,
    "needsSupportForOnlineAssessment" -> Yes,
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> "",
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario7Map = Map[String, String](
    "hasDisability" -> No,
    "hasDisabilityDescription" -> "",
    // guaranteedInterview missing
    "needsSupportForOnlineAssessment" -> Yes,
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> Yes,
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario8Map = Map[String, String](
    "hasDisability" -> No,
    "hasDisabilityDescription" -> "",
    "guaranteedInterview" -> No,
    "needsSupportForOnlineAssessment" -> Yes,
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> Yes,
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario9Map = Map[String, String](
    "hasDisability" -> "I don't know/prefer not to say",
    "hasDisabilityDescription" -> "",
    // guaranteedInterview missing
    "needsSupportForOnlineAssessment" -> Yes,
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> Yes,
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario10Map = Map[String, String](
    "hasDisability" -> "I don't know/prefer not to say",
    "hasDisabilityDescription" -> "",
    // guaranteedInterview missing
    // needsSupportForOnlineAssessment missing
    "needsSupportForOnlineAssessmentDescription" -> "",
    "needsSupportAtVenue" -> Yes,
    "needsSupportAtVenueDescription" -> ""
  )

  val Scenario11Map = Map[String, String](
    "hasDisability" -> No,
    "hasDisabilityDescription" -> "",
    // guaranteedInterview missing
    // needsSupportForOnlineAssessment missing
    "needsSupportForOnlineAssessmentDescription" -> "",
    // needsSupportAtVenue missing
    "needsSupportAtVenueDescription" -> ""
  )

  val DisabilityNoGisAndAdjustmentsMap = Map[String, String](
    "hasDisability" -> Yes,
    "hasDisabilityDescription" -> "Epilepsy",
    "guaranteedInterview" -> No,
    "needsSupportForOnlineAssessment" -> Yes,
    "needsSupportForOnlineAssessmentDescription" -> "Some adjustment",
    "needsSupportAtVenue" -> Yes,
    "needsSupportAtVenueDescription" -> "Some other adjustments")

  val DisabilityGisAndAdjustmentsFormUrlEncodedBody = Seq(
    "hasDisability" -> Yes,
    "hasDisabilityDescription" -> "Epilepsy",
    "guaranteedInterview" -> Yes,
    "needsSupportForOnlineAssessment" -> Yes,
    "needsSupportForOnlineAssessmentDescription" -> "Some adjustment",
    "needsSupportAtVenue" -> Yes,
    "needsSupportAtVenueDescription" -> "Some other adjustments")
}
