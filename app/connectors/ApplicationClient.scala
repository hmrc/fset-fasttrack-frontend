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

package connectors

import config.CSRHttp
import connectors.AllocationExchangeObjects._
import connectors.ExchangeObjects._
import connectors.exchange.{ LocationSchemes, ProgressResponse, Questionnaire, SchemeInfo, _ }
import forms.GeneralDetailsForm
import mappings.PostCodeMapping
import models.UniqueIdentifier
import org.joda.time.LocalDate
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ BadRequestException, HeaderCarrier, HttpResponse, NotFoundException }

trait ApplicationClient {

  val http: CSRHttp

  import ApplicationClient._
  import ExchangeObjects.Implicits._
  import config.FrontendAppConfig.fasttrackConfig._

  protected lazy val hostBase: LoginInfo = url.host + url.base

  def createApplication(userId: UniqueIdentifier, frameworkId: String)
    (implicit hc: HeaderCarrier): Future[ApplicationResponse] = {

    http.PUT(s"${url.host}${url.base}/application/create", CreateApplicationRequest(userId, frameworkId)).map { response =>
      response.json.as[ApplicationResponse]
    }
  }

  def submitApplication(userId: UniqueIdentifier, applicationId: UniqueIdentifier)
    (implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"${url.host}${url.base}/application/submit/$userId/$applicationId", Json.toJson("")).map {
      case x: HttpResponse if x.status == OK => ()
    }.recover {
      case _: BadRequestException => throw new CannotSubmit()
    }
  }

  def withdrawApplication(applicationId: UniqueIdentifier, reason: WithdrawApplicationRequest)
    (implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"${url.host}${url.base}/application/withdraw/$applicationId", Json.toJson(reason)).map {
      case x: HttpResponse if x.status == OK => ()
    }.recover {
      case _: NotFoundException => throw new CannotWithdraw()
    }
  }

  def addMedia(userId: UniqueIdentifier, media: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"${url.host}${url.base}/media/create", AddMedia(userId, media)).map {
      case x: HttpResponse if x.status == CREATED => ()
    } recover {
      case _: BadRequestException => throw new CannotAddMedia()
    }
  }

  def getApplicationProgress(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[ProgressResponse] = {
    http.GET(s"${url.host}${url.base}/application/progress/$applicationId").map { response =>
      response.json.as[ProgressResponse]
    }
  }

  def getApplication(userId: UniqueIdentifier, frameworkId: String)(implicit hc: HeaderCarrier): Future[ApplicationResponse] = {
    http.GET(s"${url.host}${url.base}/application/find/user/$userId/framework/$frameworkId").map { response =>
      response.json.as[ApplicationResponse]
    } recover {
      case _: NotFoundException => throw new ApplicationNotFound()
    }
  }

  def updatePersonalDetails(applicationId: UniqueIdentifier, userId: UniqueIdentifier, data: GeneralDetailsForm.Data,
                            email: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST(
      s"${url.host}${url.base}/personal-details/$userId/$applicationId",
      PersonalDetails(
        data.firstName,
        data.lastName,
        data.preferredName,
        email,
        LocalDate.parse(s"${data.dateOfBirth.year}-${data.dateOfBirth.month}-${data.dateOfBirth.day}"),
        data.outsideUk.getOrElse(false),
        data.address,
        PostCodeMapping.formatPostcode(data.postCode),
        data.country,
        data.phone,
        data.aLevel.getOrElse(false),
        data.stemLevel.getOrElse(false),
        data.civilServant == "Yes",
        data.department
      )
    ).map {
        case x: HttpResponse if x.status == CREATED => ()
      } recover {
        case _: BadRequestException => throw new CannotUpdateRecord()
      }
  }

  def getPersonalDetails(userId: UniqueIdentifier, applicationId: UniqueIdentifier)
                        (implicit hc: HeaderCarrier): Future[PersonalDetails] = {
    http.GET(s"${url.host}${url.base}/personal-details/$userId/$applicationId").map { response =>
      response.json.as[PersonalDetails]
    } recover {
      case e: NotFoundException => throw new PersonalDetailsNotFound()
    }
  }

  def updateAssistanceDetails(applicationId: UniqueIdentifier, userId: UniqueIdentifier, assistanceDetails: AssistanceDetails)(
    implicit
    hc: HeaderCarrier
  ) = {
    http.PUT(s"${url.host}${url.base}/assistance-details/$userId/$applicationId",assistanceDetails).map {
        case x: HttpResponse if x.status == CREATED => ()
      } recover {
        case _: BadRequestException => throw new CannotUpdateRecord()
      }
  }

  def getAssistanceDetails(userId: UniqueIdentifier, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier) = {
    http.GET(s"${url.host}${url.base}/assistance-details/$userId/$applicationId").map { response =>
      response.json.as[AssistanceDetails]
    } recover {
      case _: NotFoundException => throw new AssistanceDetailsNotFound()
    }
  }

  def updateQuestionnaire(applicationId: UniqueIdentifier, sectionId: String, questionnaire: Questionnaire)
    (implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(
      s"${url.host}${url.base}/questionnaire/$applicationId/$sectionId",
      questionnaire
    ).map {
        case x: HttpResponse if x.status == ACCEPTED => ()
      } recover {
        case _: BadRequestException => throw new CannotUpdateRecord()
      }
  }

  def updateReview(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(
      s"${url.host}${url.base}/application/review/$applicationId",
      ReviewRequest(true)
    ).map {
        case x: HttpResponse if x.status == OK => ()
      } recover {
        case _: BadRequestException => throw new CannotUpdateRecord()
      }
  }

  def getAllocationDetails(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[AllocationDetails]] = {
    import AllocationExchangeObjects.Implicits._
    http.GET(s"${url.host}${url.base}/allocation-status/$appId").map { response =>
      Some(response.json.as[AllocationExchangeObjects.AllocationDetails])
    } recover {
      case _: NotFoundException => None
    }
  }

  def confirmAllocation(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST(s"${url.host}${url.base}/allocation-status/confirm/$appId", "").map(_ => ())
  }

  def getEligibleSchemeLocations(applicationId: UniqueIdentifier, latitudeOpt: Option[Double], longitudeOpt: Option[Double])
                                         (implicit hc: HeaderCarrier): Future[List[GeoLocationSchemeResult]] = {
    val optionalLocation = (for {
      latitude <- latitudeOpt
      longitude <- longitudeOpt
    } yield {
      s"&latitude=$latitude&longitude=$longitude"
    }).getOrElse("")

    http.GET(s"$hostBase/scheme-locations/eligible?applicationId=$applicationId$optionalLocation").map { response =>
      response.json.as[List[GeoLocationSchemeResult]]
    } recover {
      case ex: Throwable => throw new ErrorRetrievingEligibleLocationSchemes(ex)
    }
  }

  def saveLocationChoices(applicationId: UniqueIdentifier, locationIds: List[String])(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"$hostBase/scheme-locations/$applicationId", locationIds).map(_ => ())
  }

  def saveSchemeChoices(applicationId: UniqueIdentifier, schemeNames: List[String])(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT(s"$hostBase/schemes/$applicationId", schemeNames).map(_ => ())
  }

  def removeSchemeLocations(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.DELETE(s"$hostBase/scheme-locations/remove/$applicationId").map(_ => ())
  }

  def removeSchemes(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.DELETE(s"$hostBase/schemes/remove/$applicationId").map(_ => ())
  }

  def getSchemeLocationChoices(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[List[LocationSchemes]] = {
    http.GET(s"$hostBase/scheme-locations/$applicationId").map { response =>
      response.json.as[List[LocationSchemes]]
    } recover {
      case _: NotFoundException => throw new SchemeLocationChoicesNotFound()
    }
  }

  def getSchemeChoices(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[List[SchemeInfo]] = {
    http.GET(s"$hostBase/schemes/$applicationId").map { response =>
      response.json.as[List[SchemeInfo]]
    } recover {
      case _: NotFoundException => throw new SchemeChoicesNotFound()
    }
  }

  def getEligibleSchemes(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[List[SchemeInfo]] = {
    http.GET(s"$hostBase/schemes/eligible/$applicationId").map { response =>
      response.json.as[List[SchemeInfo]]
    } recover {
      case ex: Throwable => throw new ErrorRetrievingAvailableSchemes(ex)
    }
  }

  def getAvailableSchemes(implicit hc: HeaderCarrier): Future[List[SchemeInfo]] = {
    http.GET(s"$hostBase/schemes/available").map { response =>
      response.json.as[List[SchemeInfo]]
    }
  }

  def getCandidateScores(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[CandidateScoresAndFeedback]] = {
    http.GET(s"$hostBase/test-scores-feedback/reviewer/application/$applicationId").map { response =>
      if (response.status == OK) {
        response.json.asOpt[CandidateScoresAndFeedback]
      } else {
        throw new NotFoundException(s"Error retrieving candidate scores for $applicationId")
      }
    }
  }

  def getAssessmentCentreCompetencyAverageResult(applicationId: UniqueIdentifier)(
    implicit hc: HeaderCarrier): Future[CompetencyAverageResult] = {
    http.GET(s"$hostBase/test-scores/competency-average/application/$applicationId").map { response =>
      if (response.status == OK) {
        response.json.as[CompetencyAverageResult]
      } else {
        throw new NotFoundException(s"Error retrieving assessment centre competency average for $applicationId")
      }
    }
  }
}

object ApplicationClient extends ApplicationClient {

  override val http = CSRHttp

  sealed class CannotUpdateRecord extends Exception

  sealed class CannotSubmit extends Exception

  sealed class PersonalDetailsNotFound extends Exception

  sealed class SchemeLocationChoicesNotFound extends Exception

  sealed class SchemeChoicesNotFound extends Exception

  sealed class AssistanceDetailsNotFound extends Exception

  sealed class ApplicationNotFound extends Exception

  sealed class CannotAddMedia extends Exception

  sealed class CannotWithdraw extends Exception

  sealed class ErrorRetrievingEligibleLocationSchemes(throwable: Throwable) extends Exception(throwable)

  sealed class ErrorRetrievingLocationSchemes(throwable: Throwable) extends Exception(throwable)

  sealed class ErrorRetrievingAvailableSchemes(throwable: Throwable) extends Exception(throwable)

  sealed class ErrorRetrievingSchemes(throwable: Throwable) extends Exception(throwable)
}
