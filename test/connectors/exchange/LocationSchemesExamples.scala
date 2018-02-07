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

object LocationSchemesExamples {
  val LocationSchemes1 = LocationSchemes("id1", "testLocation1",
    schemes = List("SchemeNoALevels", "SchemeALevels", "SchemeALevelsStem"))
  val LocationSchemes2 = LocationSchemes("id2", "testLocation2",
    schemes = List("SchemeNoALevels", "SchemeALevels", "SchemeALevelsStem"))
  val LocationSchemes3 = LocationSchemes("id3", "testLocation3",
    schemes = List("SchemeNoALevels", "SchemeALevels", "SchemeALevelsStem"))
  val LocationSchemes4 = LocationSchemes("id4", "testLocation4",
    schemes = List("SchemeNoALevels", "SchemeALevels", "SchemeALevelsStem"))
}
