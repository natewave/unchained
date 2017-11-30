import Dependencies._

lazy val root = (project in file(".")).
  settings(Seq(
    organization := "io.github.natewave",
    scalaVersion := "2.12.4",
    version      := "0.1.0-SNAPSHOT",
    name := "Unchained") ++ Scalac.settings ++ Seq(
    libraryDependencies ++= specsDepsTest ++ Seq(
      akkaStream, akkaSlf4J, logback,
      akkaStreamContrib % Test, akkaStreamTestKit % Test),
      fork in Test := true
  ) ++ Scalariform.settings ++ Scapegoat.settings)
