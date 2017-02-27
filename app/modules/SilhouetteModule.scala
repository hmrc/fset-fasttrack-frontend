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

package modules

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.crypto.{ JcaCookieSigner, JcaCookieSignerSettings, JcaCrypter, JcaCrypterSettings }
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{ CookieStateProvider, CookieStateSettings, DummyStateProvider }
import com.mohiva.play.silhouette.impl.providers.openid.YahooProvider
import com.mohiva.play.silhouette.impl.providers.openid.services.PlayOpenIDService
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import config.{ CustomSecuredErrorHandler, SecurityEnvironmentImpl }
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.{ Configuration, Play }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.openid.OpenIdClient
import play.api.libs.ws.WSClient
import security.{ CsrCredentialsProvider, SecurityEnvironment }
import scala.concurrent.duration._
import language.postfixOps

/**
  * The Guice module which wires all Silhouette dependencies.
  */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
    * Configures the module.
    */
  def configure() {
    bind[Silhouette[SecurityEnvironment]].to[SilhouetteProvider[SecurityEnvironment]]
    // bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    // bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    // bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    // Replace this with the bindings to your concrete DAOs
    /*bind[DelegableAuthInfoDAO[PasswordInfo]].toInstance(new InMemoryAuthInfoDAO[PasswordInfo])
    bind[DelegableAuthInfoDAO[OAuth1Info]].toInstance(new InMemoryAuthInfoDAO[OAuth1Info])
    bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])
    bind[DelegableAuthInfoDAO[OpenIDInfo]].toInstance(new InMemoryAuthInfoDAO[OpenIDInfo])*/
  }

  /**
    * Provides the HTTP layer implementation.
    *
    * @param client Play's WS client.
    * @return The HTTP layer implementation.
    */
  // @Provides
  // def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment.
    *
    * @param authenticatorService The authentication service implementation.
    * @param eventBus The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment( userService: UserService,
                          authenticatorService: AuthenticatorService[SessionAuthenticator],
                          eventBus: EventBus): Environment[SecurityEnvironment] = {

    Environment[SecurityEnvironment](
      SecurityEnvironmentImpl.userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
    * Provides the authenticator service.
    *
    * @param fingerprintGenerator The fingerprint generator implementation.
    * @param idGenerator The ID generator implementation.
    * @param configuration The Play configuration.
    * @param clock The clock instance.
    * @return The authenticator service.
    */
  @Provides
  def provideAuthenticatorService(
                                   fingerprintGenerator: FingerprintGenerator,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock): AuthenticatorService[SessionAuthenticator] = {
    new SessionAuthenticatorService(SessionAuthenticatorSettings(
      sessionKey = configuration.getString("silhouette.authenticator.sessionKey").get,
      useFingerprinting = configuration.getBoolean("silhouette.authenticator.useFingerprinting").get,
      authenticatorIdleTimeout = configuration.getInt("silhouette.authenticator.authenticatorIdleTimeout").map(x => x seconds),
      authenticatorExpiry = configuration.getInt("silhouette.authenticator.authenticatorExpiry").get seconds
    ), new DefaultFingerprintGenerator(false), new Base64AuthenticatorEncoder, Clock())
  }
}
