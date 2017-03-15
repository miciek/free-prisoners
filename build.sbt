name := "free-prisoners"

organization := "miciek"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.1"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  val catsV = "0.9.0"
  val akkaV = "2.4.17"
  val configV = "1.3.1"
  val scalatest = "3.0.1"
  Seq(
    "org.typelevel" %% "cats" % catsV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-remote" % akkaV,
    "com.typesafe" % "config" % configV,
    "org.scalatest" %% "scalatest" % scalatest % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % Test
  )
}

fork := true
connectInput in run := true

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(AlignArguments, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
  .setPreference(RewriteArrowSymbols, true)
