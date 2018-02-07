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

package forms

import forms.AssistanceDetailsForm.Data
import play.api.data.Form
import testkit.UnitWithAppSpec

class AssistanceDetailsFormSpec extends UnitWithAppSpec {

  "the assistance details form" should {
    "be valid when the user selects no to disability" in new Fixture {
      val (data, form) = NoDisabilities
      form.get mustBe data
    }

    "be valid when the user selects yes to disability and no to adjustment" in new Fixture {
      val (data, form) = NoAdjustments
      form.get mustBe data
    }

    "be valid when the user fills in the full form" in new Fixture {
      val (data, form) = Full
      form.get mustBe data
    }

    "be valid for scenario 1: disability=Y, gis=Y, online support not answered" in new Fixture {
      assertNoFormErrors(AssistanceDetailsFormExamples.Scenario1Map)
    }

    "be invalid for scenario 2: disability=N, gis=N, online support not answered" in new Fixture {
      assertFormError(Seq(
        "error.needsSupportForOnlineAssessment.required",
        "error.needsSupportAtVenue.required"
      ), AssistanceDetailsFormExamples.Scenario2Map)
    }

    "be invalid for scenario 3: disability not answered, gis not answered, online support not answered" in new Fixture {
      assertFormError(Seq(
        "error.hasDisability.required",
        "error.needsSupportForOnlineAssessment.required",
        "error.needsSupportAtVenue.required"
      ), AssistanceDetailsFormExamples.Scenario3Map)
    }

    "be invalid for scenario 4: disability=Y, gis not answered, online support not answered" in new Fixture {
      assertFormError(Seq(
        "Tell us if you wish to apply under the Guaranteed interview scheme",
        "error.needsSupportForOnlineAssessment.required",
        "error.needsSupportAtVenue.required"
      ), AssistanceDetailsFormExamples.Scenario4Map)
    }

    "be invalid for scenario 5: disability=Y, gis=N, online support not answered" in new Fixture {
      assertFormError(Seq(
        "error.needsSupportForOnlineAssessment.required",
        "error.needsSupportAtVenue.required"
      ), AssistanceDetailsFormExamples.Scenario5Map)
    }

    "be invalid for scenario 6: disability=Y, gis=N, online support=Y" in new Fixture {
      assertFormError(Seq(
        "Tell us what adjustments you need for your online tests",
        "error.needsSupportAtVenue.required"
      ), AssistanceDetailsFormExamples.Scenario6Map)
    }

    "be invalid for scenario 7: disability=N, gis not answered, online support=Y" in new Fixture {
      assertFormError(Seq(
        "Tell us what adjustments you need for your online tests",
        "Tell us what adjustments you need at any of our venues"
      ), AssistanceDetailsFormExamples.Scenario7Map)
    }

    "be invalid for scenario 8: disability=N, gis=N, online support=Y" in new Fixture {
      assertFormError(Seq(
        "Tell us what adjustments you need for your online tests",
        "Tell us what adjustments you need at any of our venues"
      ), AssistanceDetailsFormExamples.Scenario8Map)
    }

    "be invalid for scenario 9: disability=I don't know/prefer not to say, gis not answered, online support=Y" in new Fixture {
      assertFormError(Seq(
        "Tell us what adjustments you need for your online tests",
        "Tell us what adjustments you need at any of our venues"
      ), AssistanceDetailsFormExamples.Scenario9Map)
    }

    "be invalid for scenario 10: disability=I don't know/prefer not to say, gis not answered, online support not answered" in new Fixture {
      assertFormError(Seq(
        "error.needsSupportForOnlineAssessment.required",
        "Tell us what adjustments you need at any of our venues"
      ), AssistanceDetailsFormExamples.Scenario10Map)
    }

    "be invalid for scenario 11: disability=I don't know/prefer not to say, gis not answered, online support not answered" in new Fixture {
      assertFormError(Seq(
        "error.needsSupportForOnlineAssessment.required",
        "error.needsSupportAtVenue.required"
      ), AssistanceDetailsFormExamples.Scenario11Map)
    }

    val hasDisabilityAnswerKey = "hasDisability"
    val gisAnswerKey = "guaranteedInterview"
    val supportForOnlineAssessmentAnswerKey = "needsSupportForOnlineAssessment"
    val supportForOnlineAssessmentDescriptionAnswerKey = "needsSupportForOnlineAssessmentDescription"
    val supportAtVenueAnswerKey = "needsSupportAtVenue"
    val supportAtVenueDescriptionAnswerKey = "needsSupportAtVenueDescription"

    "be invalid when has disabilities is not selected" in new Fixture {
      assertFormError(Seq(
        "error.hasDisability.required"
      ), AssistanceDetailsFormExamples.DisabilityNoGisAndAdjustmentsMap - hasDisabilityAnswerKey)
    }

    "be invalid when has disabilities and gis is not selected" in new Fixture {
      assertFormError(Seq(
        "Tell us if you wish to apply under the Guaranteed interview scheme"
      ), AssistanceDetailsFormExamples.DisabilityNoGisAndAdjustmentsMap - gisAnswerKey)
    }

    "be invalid when online test adjustments are not selected" in new Fixture {
      assertFormError(Seq(
        "error.needsSupportForOnlineAssessment.required"
      ), AssistanceDetailsFormExamples.DisabilityNoGisAndAdjustmentsMap - supportForOnlineAssessmentAnswerKey)
    }

    "be invalid when online test adjustments is selected but no description provided" in new Fixture {
      assertFormError(Seq(
        "Tell us what adjustments you need for your online tests"
      ), AssistanceDetailsFormExamples.DisabilityNoGisAndAdjustmentsMap - supportForOnlineAssessmentDescriptionAnswerKey)
    }

    "be invalid when venue adjustments are not selected" in new Fixture {
      assertFormError(Seq(
        "error.needsSupportAtVenue.required"
      ), AssistanceDetailsFormExamples.DisabilityNoGisAndAdjustmentsMap - supportAtVenueAnswerKey)
    }

    "be invalid when venue adjustments is selected but no description provided" in new Fixture {
      assertFormError(Seq(
        "Tell us what adjustments you need at any of our venues"
      ), AssistanceDetailsFormExamples.DisabilityNoGisAndAdjustmentsMap - supportAtVenueDescriptionAnswerKey)
    }
  }

  trait Fixture {
    val NoDisabilities = (AssistanceDetailsFormExamples.NoDisabilitiesForm, AssistanceDetailsForm.form.fill(
      AssistanceDetailsFormExamples.NoDisabilitiesForm))

    val NoAdjustments = (AssistanceDetailsFormExamples.NoAdjustmentsForm, AssistanceDetailsForm.form.fill(
      AssistanceDetailsFormExamples.NoAdjustmentsForm))

    val Full = (AssistanceDetailsFormExamples.FullForm, AssistanceDetailsForm.form.fill(
      AssistanceDetailsFormExamples.FullForm))

    def assertFormError(expectedErrors: Seq[String], invalidFormValues: Map[String, String]) = {
      val invalidForm: Form[Data] = AssistanceDetailsForm.form.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.message) mustBe expectedErrors
    }

    def assertNoFormErrors(formValues: Map[String, String]) = {
      val form: Form[Data] = AssistanceDetailsForm.form.bind(formValues)
      form.hasErrors mustBe false
    }
  }
}
