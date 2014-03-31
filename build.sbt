organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.0"
  val sprayV = "1.3.1"
  Seq(
    "io.spray"               %   "spray-can"     % sprayV,
    "io.spray"               %   "spray-routing" % sprayV,
    "io.spray"               %   "spray-testkit" % sprayV  % "test",
    "io.spray"               %%  "spray-json"    % "1.2.5",
    "com.typesafe.akka"      %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"      %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"             %%  "specs2-core"   % "2.3.7" % "test",
    "com.typesafe"           % "config"          % "1.2.0",
    "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.client" % "1.0.0",
    "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.jwt"    % "1.0.0"
  )
}

Revolver.settings
