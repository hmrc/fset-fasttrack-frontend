/*
 * Copyright 2018 HM Revenue & Customs
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

import connectors.exchange.{ AssessmentCentre, AssessmentScores, ProgressResponse }
import play.api.libs.json.Json

import scala.language.implicitConversions

case class Progress(
  personalDetails: Boolean,
  hasSchemeLocations: Boolean,
  hasSchemes: Boolean,
  assistanceDetails: Boolean,
  review: Boolean,
  startedQuestionnaire: Boolean,
  diversityQuestionnaire: Boolean,
  educationQuestionnaire: Boolean,
  occupationQuestionnaire: Boolean,
  submitted: Boolean,
  withdrawn: Boolean,
  onlineTest: OnlineTestProgress,
  failedToAttend: Boolean,
  assessmentScores: AssessmentScores,
  assessmentCentre: AssessmentCentre
)

case class OnlineTestProgress(
  onlineTestInvited: Boolean,
  onlineTestStarted: Boolean,
  onlineTestCompleted: Boolean,
  onlineTestExpired: Boolean,
  onlineTestAwaitingReevaluation: Boolean,
  onlineTestFailed: Boolean,
  onlineTestFailedNotified: Boolean,
  onlineTestAwaitingAllocation: Boolean,
  onlineTestAwaitingAllocationNotified: Boolean,
  onlineTestAllocationConfirmed: Boolean,
  onlineTestAllocationUnconfirmed: Boolean
)

object OnlineTestProgress {
  implicit val onlineTestProgressFormat = Json.format[OnlineTestProgress]
}

object Progress {
  implicit val assessmentScoresFormat = Json.format[AssessmentScores]
  implicit val assessmentCentreFormat = Json.format[AssessmentCentre]
  implicit val progressFormat = Json.format[Progress]

  implicit def fromProgressRespToAppProgress(progressResponse: ProgressResponse): Progress =
    Progress(
      personalDetails = progressResponse.personalDetails,
      hasSchemeLocations = progressResponse.hasSchemeLocations,
      hasSchemes = progressResponse.hasSchemes,
      assistanceDetails = progressResponse.assistanceDetails,
      review = progressResponse.review,
      startedQuestionnaire = progressResponse.questionnaire.diversityStarted,
      diversityQuestionnaire = progressResponse.questionnaire.diversityCompleted,
      educationQuestionnaire = progressResponse.questionnaire.educationCompleted,
      occupationQuestionnaire = progressResponse.questionnaire.occupationCompleted,
      submitted = progressResponse.submitted,
      withdrawn = progressResponse.withdrawn,
      onlineTest = OnlineTestProgress(
        onlineTestInvited = progressResponse.onlineTest.invited,
        onlineTestStarted = progressResponse.onlineTest.started,
        onlineTestCompleted = progressResponse.onlineTest.completed,
        onlineTestExpired = progressResponse.onlineTest.expired,
        onlineTestAwaitingReevaluation = progressResponse.onlineTest.awaitingReevaluation,
        onlineTestFailed = progressResponse.onlineTest.failed,
        onlineTestFailedNotified = progressResponse.onlineTest.failedNotified,
        onlineTestAwaitingAllocation = progressResponse.onlineTest.awaitingAllocation,
        onlineTestAwaitingAllocationNotified = progressResponse.onlineTest.awaitingAllocationNotified,
        onlineTestAllocationConfirmed = progressResponse.onlineTest.allocationConfirmed,
        onlineTestAllocationUnconfirmed = progressResponse.onlineTest.allocationUnconfirmed
      ),
      failedToAttend = progressResponse.failedToAttend,
      assessmentScores = progressResponse.assessmentScores,
      assessmentCentre = progressResponse.assessmentCentre
    )
}
