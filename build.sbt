name := "wiremock-json-extractor"

version := "0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.github.tomakehurst" % "wiremock" % "1.57" % "provided",
  "io.gatling" % "jsonpath_2.11" % "0.6.4",

  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" % "test"
)
    