name := "free-prisoners"

organization := "miciek"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.3"

resolvers += Resolver.jcenterRepo
resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin(
  "org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")

libraryDependencies ++= {
  val freestyleV = "0.4.4"
  val catsEffectV = "0.4"
  val akkaV = "2.5.7"
  val configV = "1.3.2"
  val scalatestV = "3.0.1"
  Seq(
    "io.frees" %% "frees-core" % freestyleV,
    "org.typelevel" %% "cats-effect" % catsEffectV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-remote" % akkaV,
    "com.typesafe" % "config" % configV,
    "org.scalatest" %% "scalatest" % scalatestV % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % Test
  )
}

scalacOptions ++= List("-unchecked",
                       "-Ywarn-unused-import",
                       "-Xfatal-warnings",
                       "-Ypartial-unification",
                       "-language:higherKinds",
                       "-Xlint")

fork := true
connectInput in run := true
scalafmtVersion in ThisBuild := "1.3.0"
scalafmtOnCompile in ThisBuild := true
