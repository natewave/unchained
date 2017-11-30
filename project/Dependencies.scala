import sbt._

object Dependencies {
  val specsVer = "4.0.1"

  val specsDeps = Seq("specs2-core", "specs2-junit").
    map("org.specs2" %% _ % specsVer)

  @inline def specsDepsTest = specsDeps.map(_ % Test)

  val akkaVer = "2.5.4"
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVer
  val akkaSlf4J = "com.typesafe.akka" %% "akka-slf4j" % akkaVer
  val akkaStreamContrib = "com.typesafe.akka" %% "akka-stream-contrib" % "0.8"

  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
