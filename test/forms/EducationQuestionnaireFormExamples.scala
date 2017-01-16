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

object EducationQuestionnaireFormExamples {

  val FullValidForm = EducationQuestionnaireForm.Data(
    liveInUKBetween14and18 = "Yes",
    postcode = Some("AAA 111"),
    preferNotSayPostcode = None,
    schoolName14to16 = Some("my school at 15"),
    schoolId14to16 = None,
    preferNotSaySchoolName14to16 = None,
    schoolName16to18 = Some("my school at 17"),
    schoolId16to18 = None,
    preferNotSaySchoolName16to18 = None,
    freeSchoolMeals = Some("No")
  )

  val AllPreferNotToSayValidForm = EducationQuestionnaireForm.Data(
    liveInUKBetween14and18 = "Yes",
    postcode = None,
    preferNotSayPostcode = Some(true),
    schoolName14to16 = None,
    schoolId14to16 = None,
    preferNotSaySchoolName14to16 = Some(true),
    schoolName16to18 = None,
    schoolId16to18 = None,
    preferNotSaySchoolName16to18 = Some(true),
    freeSchoolMeals = Some("I don't know/prefer not to say")
  )

  val FullValidFormMap = Map(
    "liveInUKBetween14and18" -> "Yes",
    "postcodeQ" -> "SL1 3GQ",
    "schoolName14to16" -> "my school at 15",
    "schoolName16to18" -> "my school at 17",
    "freeSchoolMeals" -> "No",
    "isCandidateCivilServant" -> "No"
  )

  val AllPreferNotToSayFormMap = Map(
    "liveInUKBetween14and18" -> "Yes",
    "preferNotSayPostcode" -> "Yes",
    "preferNotSaySchoolName14to16" -> "true",
    "preferNotSaySchoolName16to18" -> "true",
    "freeSchoolMeals" -> "I don't know/prefer not to say",
    "isCandidateCivilServant" -> "No"
  )
}
