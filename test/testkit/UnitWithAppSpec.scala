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

package testkit

import akka.stream.Materializer
import com.google.inject.AbstractModule
import com.kenshoo.play.metrics.{ MetricsFilter, PlayModule }
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.test.FakeEnvironment
import controllers.UnitSpec
import models.SecurityUser
import net.codingwell.scalaguice.ScalaModule
import org.scalatestplus.play.OneAppPerSuite
import play.api.{ Application, Play }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsValue, Json, Writes }
import play.api.test.{ FakeApplication, FakeHeaders, FakeRequest }
import security.{ SecurityEnvironment, SilhouetteComponent }

abstract class UnitWithAppSpec extends UnitSpec with OneAppPerSuite {

  // Suppress logging during tests
  val additionalConfig = Map("logger.application" -> "ERROR")

  val AppId = "AppId"
  val UserId = "UserId"

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(new SilhouetteFakeModule())
    .disable[PlayModule]
    .build

  implicit def mat: Materializer = Play.materializer(app)
}
