lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion     = "2.6.3"
lazy val circeVersion    = "0.13.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.liquidcore7",
      scalaVersion    := "2.13.1"
    )),
    mainClass in assembly := Some("com.liquidcore7.randombackend.HttpApp"),
    name := "randombackend",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"      % akkaVersion,

      "ch.megard"         %% "akka-http-cors"   % "0.4.2",
      "de.heikoseeberger" %% "akka-http-circe"  % "1.31.0",
      "ch.qos.logback"    % "logback-classic"   % "1.2.3",

      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.0.8"         % Test
    )
  )
