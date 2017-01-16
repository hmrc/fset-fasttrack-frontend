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

import controllers.UnitSpec
import forms.EducationQuestionnaireFormExamples._
import play.api.data.Form

class EducationQuestionnaireFormSpec extends UnitSpec {

  "The diversity education form" should {
    "be valid when all values are correct" in new Fixture {
      val (data, form) = FullValid
      form.get mustBe data
    }

    "be valid when all possible values are <<I don't know/ prefer not to say>>" in new Fixture {
      val (data, form) = AllPreferNotToSayValid
      form.get mustBe data
    }

    "fail when all values are correct but not lived in UK" in new Fixture {
      assertFieldRequired(FullValidFormMap, "liveInUKBetween14and18", "liveInUKBetween14and18")
    }

    "fail when all values are correct but no postcodeQ" in new Fixture {
      assertFieldRequired(FullValidFormMap, "postcodeQ", "postcodeQ")
    }

    "fail when all values are correct but no schoolName14to16" in new Fixture {
      assertFieldRequired(FullValidFormMap, "schoolName14to16", "schoolName14to16")
    }

    "fail when all values are correct but no schoolName16to18" in new Fixture {
      assertFieldRequired(FullValidFormMap, "schoolName16to18", "schoolName16to18")
    }

    "fail when all values are correct but no freeSchoolMeals" in new Fixture {
      assertFieldRequired(FullValidFormMap, "freeSchoolMeals", "freeSchoolMeals")
    }

    "transform form when form is full valid (has degree and lived in uk) to a question list" in new Fixture {
      val questionList = FullValidForm.exchange.questions
      questionList.size mustBe 5
      questionList(0).answer.answer mustBe Some("Yes")
      questionList(0).answer.unknown mustBe None
      questionList(1).answer.answer mustBe Some("AAA 111")
      questionList(1).answer.unknown mustBe None
      questionList(2).answer.answer mustBe Some("my school at 15")
      questionList(2).answer.unknown mustBe None
      questionList(3).answer.answer mustBe Some("my school at 17")
      questionList(3).answer.unknown mustBe None
      questionList(4).answer.answer mustBe Some("No")
      questionList(4).answer.unknown mustBe None
    }

    "transform form when has degree with all possible fields with prefer not to say" in new Fixture {
      val questionList = AllPreferNotToSayValidForm.exchange.questions
      questionList.size mustBe 5
      questionList(0).answer.answer mustBe Some("Yes")
      questionList(0).answer.unknown mustBe None
      questionList(1).answer.answer mustBe None
      questionList(1).answer.unknown mustBe Some(true)
      questionList(2).answer.answer mustBe None
      questionList(2).answer.unknown mustBe Some(true)
      questionList(3).answer.answer mustBe None
      questionList(3).answer.unknown mustBe Some(true)
      questionList(4).answer.answer mustBe None
      questionList(4).answer.unknown mustBe Some(true)
    }

    "sanitize data should respect values when liveInUKBetween14and18 is Yes and haveDegree is Yes" in new Fixture {
      EducationQuestionnaireFormExamples.FullValidForm.sanitizeData mustBe EducationQuestionnaireFormExamples.FullValidForm
    }
  }

  trait Fixture {
    val fastStreamForm = EducationQuestionnaireForm.form

    val FullValid = (EducationQuestionnaireFormExamples.FullValidForm, fastStreamForm.fill(
      EducationQuestionnaireFormExamples.FullValidForm))

    val AllPreferNotToSayValid = (EducationQuestionnaireFormExamples.AllPreferNotToSayValidForm, fastStreamForm.fill(
      EducationQuestionnaireFormExamples.AllPreferNotToSayValidForm))

    def assertFieldRequired(formMap: Map[String, String], expectedKeyInError: String, fieldKey: String*) =
      assertFormError(expectedKeyInError, formMap ++ fieldKey.map(k => k -> ""), fastStreamForm)

    def assertFormError(expectedKey: String, invalidFormValues: Map[String, String], form: Form[EducationQuestionnaireForm.Data]) = {
      val invalidForm = form.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.key) mustBe Seq(expectedKey)
    }
  }
}
