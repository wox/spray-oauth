package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

class MyServiceActor extends Actor with MyService {
  def actorRefFactory = context
  def receive = runRoute(myRoute ~ authRoute)
}

trait MyService extends HttpService with AuthService with InMemorySessionStore {

  val getWithSession = get & session

  val myRoute =
    (path("") & get) {
      complete {
        <html>
          <body>
            <h1>Welcome!</h1>
            <p><a href='oauth2redirect?method=google'>Login (Google)</a></p>
            <p><a href='oauth2logout'>Logout</a></p>
            <p><a href='email'>Get Email</a></p>
          </body>
        </html>
      }
    } ~
    (path("email") & getWithSession) { user =>
      complete {
        Auth.Google.requestEmail(user)
      }
    }
}