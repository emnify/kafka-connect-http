import sbt.Keys.crossScalaVersions

lazy val scala212               = "2.12.21"
lazy val scala213               = "2.13.18"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / credentials += Credentials(
  "emnify/emnify-maven",
  "emnify-648956897802.d.codeartifact.eu-west-1.amazonaws.com",
  "aws",
  sys.env.getOrElse("CODEARTIFACT_AUTH_TOKEN", "Unknown")
)

ThisBuild / scalaVersion := scala213
ThisBuild / organization := "io.github.kirill5k"
ThisBuild / organizationName := "example"
ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sbtPluginRepo("releases"),
  "Confluent" at "https://packages.confluent.io/maven/",
  "emnify/emnify-maven" at "https://emnify-648956897802.d.codeartifact.eu-west-1.amazonaws.com/maven/emnify-maven/",
)

ThisBuild / publishTo := Some("emnify/emnify-maven" at "https://emnify-648956897802.d.codeartifact.eu-west-1.amazonaws.com/maven/emnify-maven/")
ThisBuild / publishMavenStyle := true

releaseVersionBump := sbtrelease.Version.Bump.Next
releaseCrossBuild := false

lazy val root = (project in file("."))
  .settings(
    name := "kafka-connect-http",
    crossScalaVersions := Nil
  )
  .aggregate(sink)

lazy val commonSettings = Seq(
  organizationName := "Kafka Connect Http",
  startYear := Some(2020),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  resolvers += "Apache public" at "https://repository.apache.org/content/groups/public/",
  scalafmtOnCompile := true,
  crossScalaVersions := supportedScalaVersions
)

lazy val sink = (project in file("connectors/sink"))
  .settings(commonSettings)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "kafka-connect-http-sink",
    moduleName := "kafka-connect-http-sink",
    libraryDependencies ++= Dependencies.sink ++ Dependencies.test,
    // Multiple `must be` assertions per test are idiomatic in scalatest
    Test / tpolecatExcludeOptions += org.typelevel.scalacoptions.ScalacOptions.warnNonUnitStatement,
    assembly / assemblyJarName := "kafka-connect-http-sink.jar",
    // Bundle scala-library: the Connect runtime's (Kafka's) scala-library is older than the one we compile against,
    // and Connect plugin isolation loads scala.* from the plugin first.
    assembly / assemblyOption := (assembly / assemblyOption).value
      .withIncludeScala(true)
      .withIncludeDependency(true),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x                             => MergeStrategy.first
    },
    Compile / assembly / artifact := {
      val art = (Compile / assembly / artifact).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(Compile / assembly / artifact, assembly)
  )
  .enablePlugins(AutomateHeaderPlugin)
