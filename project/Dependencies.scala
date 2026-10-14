import sbt._

object Dependencies {

  object Versions {
    lazy val kafka     = "3.7.2"
    lazy val confluent = "7.7.12"
    lazy val circe     = "0.14.17"
    lazy val sttp      = "3.11.0"

    lazy val scalatest   = "3.2.20"
    lazy val mockito     = "2.2.3"
    lazy val mockserver  = "8.0.0"
    lazy val scalaCompat = "2.14.0"
  }

  object Libraries {

    lazy val scalaCompat       = "org.scala-lang.modules"        %% "scala-collection-compat"      % Versions.scalaCompat
    lazy val connectApi        = "org.apache.kafka"               % "connect-api"                  % Versions.kafka
    lazy val connectAvro       = "io.confluent"                   % "kafka-connect-avro-converter" % Versions.confluent
    lazy val connectJson       = "org.apache.kafka"               % "connect-json"                 % Versions.kafka
    lazy val sttpCore          = "com.softwaremill.sttp.client3" %% "core"                         % Versions.sttp
    lazy val sttpFutureBackend = "com.softwaremill.sttp.client3" %% "circe"                        % Versions.sttp

    lazy val circeCore    = "io.circe" %% "circe-core"    % Versions.circe
    lazy val circeParser  = "io.circe" %% "circe-parser"  % Versions.circe
    lazy val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe

    lazy val scalaTest        = "org.scalatest"  %% "scalatest"               % Versions.scalatest
    lazy val mockitoCore      = "org.mockito"    %% "mockito-scala"           % Versions.mockito
    lazy val mockitoScalatest = "org.mockito"    %% "mockito-scala-scalatest" % Versions.mockito
    lazy val mockserver       = "org.mock-server" % "mockserver-netty"        % Versions.mockserver
  }

  lazy val sink = Seq(
    Libraries.scalaCompat,
    Libraries.connectApi,
    Libraries.connectAvro,
    Libraries.connectJson,
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.sttpCore,
    Libraries.sttpFutureBackend
  )

  lazy val test = Seq(
    Libraries.scalaTest        % Test,
    Libraries.mockitoCore      % Test,
    Libraries.mockitoScalatest % Test,
    Libraries.mockserver       % Test
  )
}
