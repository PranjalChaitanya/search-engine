ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.3" // Use the latest version


lazy val root = (project in file("."))
  .settings(
    name := "search-engine"
  )
