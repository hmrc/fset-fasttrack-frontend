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

package security

import models.ApplicationData.ApplicationStatus._
import models.CachedData
import play.api.i18n.Lang
import play.api.mvc.{ Request, RequestHeader }

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

object QuestionnaireRoles {

  import Roles._
  import RoleUtils._

  object StartOrContinueQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasAssistanceDetails(user)
  }

  object QuestionnaireNotStartedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) &&
        !hasDiversityQuestionnaire(user) && !hasEducationQuestionnaire(user) && !hasParentalOccupationQuestionnaire(user)
  }

  object QuestionnaireInProgressRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasAssistanceDetails(user) &&
        (!hasDiversityQuestionnaire(user) || !hasEducationQuestionnaire(user) || !hasParentalOccupationQuestionnaire(user))
  }

  object DiversityQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasStartedQuestionnaire(user) && !hasDiversityQuestionnaire(user)
  }

  object DiversityQuestionnaireCompletedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasDiversityQuestionnaire(user)
  }

  object EducationQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasDiversityQuestionnaire(user) && !hasEducationQuestionnaire(user)
  }

  object EducationQuestionnaireCompletedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasEducationQuestionnaire(user)
  }

  object ParentalOccupationQuestionnaireRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasEducationQuestionnaire(user) && !hasParentalOccupationQuestionnaire(user)
  }

  object ParentalOccupationQuestionnaireCompletedRole extends CsrAuthorization {
    override def isAuthorized(user: CachedData)(implicit request: RequestHeader) =
      activeUserWithApp(user) && statusIn(user)(IN_PROGRESS) && hasParentalOccupationQuestionnaire(user)
  }
}
