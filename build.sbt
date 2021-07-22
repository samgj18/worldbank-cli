name := "pecunia"

version := "0.1"

scalaVersion := "2.13.6"

enablePlugins(JavaAppPackaging, PackPlugin)

packMain := Map("run" -> "Main")

scalacOptions ++= Seq(
  "-Ywarn-unused:imports",
  "-language:postfixOps"
)

lazy val doobieVersion = "1.0.0-M5"

lazy val root = (project in file("."))
  .settings(
    name := "interview",
    libraryDependencies ++= Seq(
      "ch.qos.logback"                 % "logback-classic"                % "1.2.3",
      "com.typesafe.scala-logging"    %% "scala-logging"                  % "3.9.4",
      "com.softwaremill.sttp.client3" %% "core"                           % "3.3.11",
      "com.softwaremill.sttp.client3" %% "circe"                          % "3.3.11",
      "io.circe"                      %% "circe-generic"                  % "0.14.1",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.3.11",
      "org.tpolecat"                  %% "doobie-core"                    % doobieVersion,
      "org.tpolecat"                  %% "doobie-h2"                      % doobieVersion,
      "org.typelevel"                 %% "cats-effect"                    % "3.1.1",
//      "org.slf4j"                      % "slf4j-nop"         % "1.6.4",
      "org.wvlet.airframe"            %% "airframe-launcher"              % "19.11.1"
    )
  )
