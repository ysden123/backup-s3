import java.util.Calendar

ThisBuild / scalaVersion := "3.2.1"
ThisBuild / version := "3.0.0"
ThisBuild / organization := "com.stulsoft"
ThisBuild / organizationName := "stulsoft"

lazy val json4sVersion = "4.0.6"
lazy val root = (project in file("."))
  .settings(
    name := "backup-s3",
    maintainer := "ysden123@gmail.com",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.5",
    libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.0",
    libraryDependencies += "org.json4s" %% "json4s-native" % json4sVersion,
    libraryDependencies += "org.json4s" %% "json4s-jackson" % json4sVersion,
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.12.0",

    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % Test
  )
  .enablePlugins(JavaAppPackaging)

Compile / packageBin / packageOptions += Package.ManifestAttributes("Build-Date" -> Calendar.getInstance().getTime.toString)

Compile / mainClass := Some("com.stulsoft.backup.Main")