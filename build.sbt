name := "free-prisoners"

organization := "miciek"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.3"

resolvers += Resolver.jcenterRepo

addCompilerPlugin(
  "org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)

libraryDependencies ++= {
  val freestyleV = "0.4.4"
  val akkaV = "2.5.7"
  val configV = "1.3.1"
  val scalatestV = "3.0.1"
  Seq(
    "io.frees" %% "frees-core" % freestyleV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-remote" % akkaV,
    "com.typesafe" % "config" % configV,
    "org.scalameta" %% "scalameta" % "1.8.0",
    "org.scalatest" %% "scalatest" % scalatestV % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % Test
  )
}

scalacOptions ++= List("-unchecked",
                       "-Ywarn-unused-import",
                       "-Xfatal-warnings",
                       "-Ypartial-unification",
                       "-language:higherKinds")

fork := true
connectInput in run := true
scalafmtVersion in ThisBuild := "1.3.0"
scalafmtOnCompile in ThisBuild := true
