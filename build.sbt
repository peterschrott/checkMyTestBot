import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation")

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "5.3.0"
// Extra goodies: Webhooks, support for games, bindings for actors.
libraryDependencies += "com.bot4s" %% "telegram-akka" % "5.3.0"
// Configuration
libraryDependencies += "com.typesafe" % "config" % "1.4.1"
// Algolia
libraryDependencies += "com.algolia" %% "algoliasearch-scala" % "1.44.0"
// Logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"


lazy val globalSettings = Seq(
  organization := "io.bluerootlabs",
  Test / fork := true,
  run / fork := true,
  Global / cancelable := true
)

lazy val root = (project in file("."))
  .settings(globalSettings: _*)
  .settings(name := "check-my-test-bot")

lazy val checkMyTestProd = project
  .in(file(".build/checkMyTestProd"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(dockerSettingsProd: _*)
  .settings(Compile / mainClass := Some("io.bluerootlabs.checkmytestbot.CheckMyTestBotApp"))
  .dependsOn(root)

/*
 * Extended common settings
 */
lazy val dockerSettingsProd = Seq(
  dockerPermissionStrategy := DockerPermissionStrategy.Run,
  Docker / maintainer  := "Peter Schrott <peter@bluerootlabs.io>",
  packageDescription := "Docker [micro|nano] Service",
  Docker / packageName := "bluerootlabs/checkmytest",
  Docker / version := "latest",
  dockerExposedPorts ++= Seq(8443),
  dockerEntrypoint := Seq("bin/checkmytestprod",  "-Dconfig.resource=application.prod.conf"),
)
