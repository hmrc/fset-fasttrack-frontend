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

package util;

import mappings.PostCodeMapping
import mappings.PostCodeMapping._
import org.scalatestplus.play.PlaySpec
import play.api.data.validation.Valid

class PostCodeFormatterSpec extends PlaySpec {

  "Postcode Formatter" should {

    def postcodeFormatValidation(from: Option[String], against: Option[String]) = {
      validPostcode(from.get) mustBe Valid
      val formatted = formatPostcode(from)
      formatted mustBe against
      validPostcode(formatted.get) mustBe Valid
    }

    "Format A99AA type of postcode" in {
      postcodeFormatValidation(Some("d33fi"), Some("D3 3FI"))
    }

    "Format A099AA type of postcode" in {
      postcodeFormatValidation(Some("b032az"), Some("B3 2AZ"))
    }

    "Format A999AA type of postcode" in {
      postcodeFormatValidation(Some("j321ik"), Some("J32 1IK"))
    }

    "Format A9A9AA type of postcode" in {
      postcodeFormatValidation(Some("z5i2uj"), Some("Z5I 2UJ"))
    }

    "Format AA99AA type of postcode" in {
      postcodeFormatValidation(Some("ab32qu"), Some("AB3 2QU"))
    }

    "Format AA099AA type of postcode" in {
      postcodeFormatValidation(Some("jh072ui"), Some("JH7 2UI"))

    }
    "Format AA999AA type of postcode" in {
      postcodeFormatValidation(Some("rm285qk"), Some("RM28 5QK"))
    }
    "Format AA9A9AA type of postcode" in {
      postcodeFormatValidation(Some("eh3w4uc"), Some("EH3W 4UC"))
    }
  }
}
