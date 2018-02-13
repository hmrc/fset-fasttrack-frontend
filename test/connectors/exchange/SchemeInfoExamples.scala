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

object SchemeInfoExamples {
  val NoALevelsScheme = SchemeInfo("Business", "SchemeNoALevels", requiresALevel = false, requiresALevelInStem = false,
    requiresU18Level3 = false)
  val ALevelsScheme = SchemeInfo("Commercial", "SchemeALevels", requiresALevel = true, requiresALevelInStem = false, requiresU18Level3 = false)
  val ALevelsStemScheme = SchemeInfo("ProjectDelivery", "SchemeALevelsStem", requiresALevel = true, requiresALevelInStem = true ,
    requiresU18Level3 = false)
}
