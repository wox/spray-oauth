package com.example

import org.apache.oltu.oauth2.common.{OAuth, OAuthProviderType}
import org.apache.oltu.oauth2.client.request.{OAuthBearerClientRequest, OAuthClientRequest}
import org.apache.oltu.oauth2.common.message.types.GrantType
import org.apache.oltu.oauth2.client.{URLConnectionClient, OAuthClient}
import org.apache.oltu.oauth2.client.response.{OAuthResourceResponse, OAuthJSONAccessTokenResponse}
import spray.json.{DefaultJsonProtocol, JsonParser}
import org.apache.oltu.oauth2.jwt.io.JWTReader
import com.typesafe.config._

case class UserToken(userId: String, token: String)

case class Jwt(access_token: String, token_type: String, expires_in: Int, id_token: String)
case class ClaimsSet(iss: String, sub: String, aud: String, exp: Int, nbf: String, iat: Int, jti: String, typ: String)

object JsonProtocol extends DefaultJsonProtocol {
  implicit val jwtFormat = jsonFormat4(Jwt)
  implicit val claimsSetFormat = jsonFormat8(ClaimsSet)
}

import JsonProtocol._

object Auth {
  val conf = ConfigFactory.load().getConfig("auth")
  def str(key: String) = conf.getString(key)
  def Google = GoogleAuth(str("google.clientId"), str("google.clientSecret"), str("google.scope"), str("google.callback"))
  def apply(method: String) = method match {
    case "google" => Google
  }
}

trait Auth {
  def codeUri: String
  def requestToken(code: String): UserToken
  def requestEmail(userToken: UserToken): String
}

case class GoogleAuth(clientId: String, clientSecret: String, scope: String, callback: String) extends Auth {
  val providerType = OAuthProviderType.GOOGLE

  def codeUri = {
    val request = OAuthClientRequest
      .authorizationProvider(providerType)
      .setClientId(clientId)
      .setRedirectURI(callback)
      .setResponseType("code")
      .setScope(scope)
      .buildQueryMessage()
    request.getLocationUri()
  }

  def requestToken(code: String) = {
    val request = OAuthClientRequest
      .tokenProvider(providerType)
      .setGrantType(GrantType.AUTHORIZATION_CODE)
      .setClientId(clientId)
      .setClientSecret(clientSecret)
      .setRedirectURI(callback)
      .setCode(code)
    val client = new OAuthClient(new URLConnectionClient())
    val response = client.accessToken(request.buildBodyMessage(), classOf[OAuthJSONAccessTokenResponse])
    val jwtData = JsonParser(response.getBody()).convertTo[Jwt]
    val jwt = new JWTReader().read(jwtData.id_token)
    val userId = JsonParser(jwt.getClaimsSet().toString).convertTo[ClaimsSet].sub

    UserToken(userId, response.getAccessToken)
  }

  def requestEmail(userToken: UserToken) = {
    val client = new OAuthClient(new URLConnectionClient())
    val emailRequest = new OAuthBearerClientRequest("https://www.googleapis.com/userinfo/email?alt=json")
      .setAccessToken(userToken.token).buildQueryMessage()

    val resourceResponse = client.resource(emailRequest, OAuth.HttpMethod.GET, classOf[OAuthResourceResponse])
    resourceResponse.getBody
  }
}

