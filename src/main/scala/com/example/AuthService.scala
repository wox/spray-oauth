package com.example

import spray.routing._
import spray.http.StatusCodes._
import spray.http.HttpCookie

trait SessionStore {
  def getSession(sessionId: String): Option[UserToken]
  def addSession(userToken: UserToken): String
  def deleteSession(sessionId: String)

  def uuid = java.util.UUID.randomUUID.toString // TODO: Generate secure session id
}

trait InMemorySessionStore extends SessionStore {
  var sessions: Map[String, UserToken] = Map()

  override def getSession(sessionId: String): Option[UserToken] =
    sessions.get(sessionId)
  override def addSession(userToken: UserToken) = {
    val sessionId = uuid
    sessions += sessionId -> userToken
    sessionId
  }
  override def deleteSession(sessionId: String) =
    sessions -= sessionId
}

trait AuthService extends HttpService with SessionStore {

  private implicit def executionContext = actorRefFactory.dispatcher

  val session: Directive1[UserToken] = cookie("SESSION").flatMap {
    case cookie: HttpCookie => getSession(cookie.content) match {
      case Some(user) => provide(user)
      case _ => reject(AuthorizationFailedRejection)
    }
  }

  val authRoute =
    (path("oauth2redirect") & parameters('method)) { method =>
      redirect(Auth(method).codeUri, Found)
    } ~
    (path("oauth2callback") & parameters('code)) { code =>
      val userToken = Auth.Google.requestToken(code)
      val sessionId = addSession(userToken)
      setCookie(HttpCookie("SESSION", sessionId)) & redirect("/", Found)
    } ~
    (path("oauth2logout") & get) {
      deleteCookie("SESSION") & redirect("/", Found)
    }
}