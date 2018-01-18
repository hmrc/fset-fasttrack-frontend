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

package connectors.exchange

import models.UniqueIdentifier
import play.api.libs.json.Json

case class CandidateScoresAndFeedback(
  applicationId: UniqueIdentifier,
  interview: Option[ScoresAndFeedback] = None,
  groupExercise: Option[ScoresAndFeedback] = None,
  writtenExercise: Option[ScoresAndFeedback] = None
)

object CandidateScoresAndFeedback {
  implicit val candidateScoresAndFeedbackFormat = Json.format[CandidateScoresAndFeedback]

  val Interview = "interview"
  val GroupExercise = "groupExercise"
  val WrittenExercise = "writtenExercise"

  val EnabledFieldsByExercise: Map[String, List[String]] = Map(
    Interview -> List(
      "leadingAndCommunicating",
      "deliveringAtPace",
      "changingAndImproving",
      "buildingCapabilityForAll",
      "motivationFit"
    ),
    GroupExercise -> List(
      "leadingAndCommunicating",
      "collaboratingAndPartnering",
      "makingEffectiveDecisions",
      "buildingCapabilityForAll",
      "motivationFit"
    ),
    WrittenExercise -> List(
      "leadingAndCommunicating",
      "collaboratingAndPartnering",
      "deliveringAtPace",
      "makingEffectiveDecisions",
      "changingAndImproving"
    )
  )
}
