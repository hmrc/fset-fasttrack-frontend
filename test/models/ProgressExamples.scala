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

package models

import connectors.exchange.{ AssessmentCentre, AssessmentScores }

object ProgressExamples {
  val InitialProgress = Progress(false, false, false, false, false, false, false, false, false, false, false,
    OnlineTestProgress(false, false, false, false, false, false, false, false, false, false, false),
    false, AssessmentScores(false, false), AssessmentCentre(false, false, false))

  val PersonalDetailsProgress = InitialProgress.copy(personalDetails = true)
  val SchemePreferencesProgress = PersonalDetailsProgress.copy(hasSchemes = true, hasSchemeLocations = true)
  val AssistanceDetailsProgress = SchemePreferencesProgress.copy(assistanceDetails = true)
  val StartedDiversityQuestionnaireProgress = AssistanceDetailsProgress.copy(startedQuestionnaire = true)
  val DiversityQuestionnaireProgress = AssistanceDetailsProgress.copy(startedQuestionnaire = true, diversityQuestionnaire = true)
  val ParentalOcuppationQuestionnaireProgress = DiversityQuestionnaireProgress.copy(educationQuestionnaire = true,
    occupationQuestionnaire = true)
  val ReviewProgress = ParentalOcuppationQuestionnaireProgress.copy(review = true)
  val SubmitProgress = ReviewProgress.copy(submitted = true)
  val WithdrawnAfterSubmitProgress = SubmitProgress.copy(withdrawn = true)

  val OnlineTestsInvitedNotStarted = SubmitProgress.copy(
    onlineTest = OnlineTestProgress(true, false, false, false, false, false, false, false, false, false, false),
    failedToAttend = false, assessmentScores = AssessmentScores(false, false), assessmentCentre = AssessmentCentre(false, false, false))

  val OnlineTestsStarted = OnlineTestsInvitedNotStarted.copy(
    onlineTest = OnlineTestProgress(true, true, false, false, false, false, false, false, false, false, false))

  val FullProgress = Progress(true, true, true, true, true, true, true, true, true, true, false,
    OnlineTestProgress(true, true, true, false, false, false, false, false, false, true, false),
    false, AssessmentScores(true, true), AssessmentCentre(false, true, false))
}
