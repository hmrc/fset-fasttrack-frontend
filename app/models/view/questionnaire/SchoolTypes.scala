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

package models.view.questionnaire

import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object SchoolTypes {
  val list = List(
    ("state-selective",
      "A state-run or state-funded school - Selective on academic, faith or other grounds", false),
    ("state-non-selective ", "A state-run or state-funded school - Non-selective", false),
    ("independent-bursary", "Independent or fee-paying school - Bursary", false),
    ("independent-non-bursary", "Independent or fee-paying school - No bursary", false),
    ("unknown", Messages("answer.unknown"), false)
  )
}
