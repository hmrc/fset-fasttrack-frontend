/*
 * Copyright 2019 HM Revenue & Customs
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

package connectors

import config.CSRHttp
import connectors.ExchangeObjects._
import connectors.OnlineTestClient.{ OnlineTestNotFound, PdfReportNotFoundException }
import models.ApplicationData.ApplicationStatus.ApplicationStatus
import models.UniqueIdentifier
import play.api.Play.current
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, NotFoundException }

trait OnlineTestClient {

  val http: CSRHttp

  import ApplicationClient._
  import ExchangeObjects.Implicits._
  import config.FrontendAppConfig.fasttrackConfig._

  protected lazy val hostBase: LoginInfo = url.host + url.base

  def getTestAssessment(userId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[OnlineTest] = {
    http.GET(s"${url.host}${url.base}/online-test/candidate/$userId").map { response =>
      response.json.as[ExchangeObjects.OnlineTest]
    } recover {
      case _: NotFoundException => throw new OnlineTestNotFound()
    }
  }

  def getPDFReport(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Array[Byte]] = {
    http.wS.url(s"${url.host}${url.base}/online-test/pdf-report/$applicationId").get(resp =>
      if (resp.status == 200) {
        Iteratee.consume[Array[Byte]]()
      } else {
        throw new PdfReportNotFoundException()
      }).flatMap { it => it.run }
  }

  def onlineTestUpdate(userId: UniqueIdentifier, status: ApplicationStatus)(implicit hc: HeaderCarrier): Future[Unit] = {
    val body = Json.toJson(OnlineTestStatus(status))
    http.POST(s"${url.host}${url.base}/online-test/candidate/$userId/status", body).map(_ => ())
  }

  def startOnlineTests(cubiksUserId: Int)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"${url.host}${url.base}/online-test/$cubiksUserId/start", "") map { _ =>
      ()
    } recover {
      case _ : NotFoundException => throw new OnlineTestNotFound()
    }
  }

  def completeTestByToken(token: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"${url.host}${url.base}/online-test/by-token/$token/complete", "").map( _ => ())
  }
}

object OnlineTestClient extends OnlineTestClient {

  override val http = CSRHttp

  sealed class OnlineTestNotFound extends Exception

  sealed class PdfReportNotFoundException extends Exception
}
