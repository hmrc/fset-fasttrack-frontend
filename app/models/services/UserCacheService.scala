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

package models.services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import config.CSRCache
import connectors.FrameworkId
import connectors.ApplicationClient.ApplicationNotFound
import connectors.{ ApplicationClient, UserManagementClient }
import models.{ CachedData, SecurityUser, UniqueIdentifier }
import play.api.mvc.{ Request, RequestHeader }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class UserCacheService extends UserService {

  override def retrieve(loginInfo: LoginInfo): Future[Option[SecurityUser]] =
    Future.successful(Some(SecurityUser(userID = loginInfo.providerKey)))

  override def save(user: CachedData)(implicit hc: HeaderCarrier): Future[CachedData] =
    CSRCache.cache[CachedData](user.user.userID.toString, user).map(_ => user)

  override def refreshCachedUser(userId: UniqueIdentifier)(implicit hc: HeaderCarrier, request: RequestHeader): Future[CachedData] = {
    UserManagementClient.findByUserId(userId).flatMap { userData =>
      ApplicationClient.getApplication(userId, FrameworkId).flatMap { appData =>
        val cd = CachedData(userData.toCached, Some(appData))
        save(cd)
      }.recover {
        case ex: ApplicationNotFound => CachedData(userData.toCached, None)
      }
    }
  }
}
