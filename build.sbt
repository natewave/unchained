import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.github.natewave",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Unchained",
    libraryDependencies += specs2 % Test,
    scalacOptions in Test ++= Seq("-Yrangepos")
  )
