name            := "wiremock-json-extractor"
organization    := "tv.teads"
scalaVersion    := "2.11.7"
scalacOptions   := Seq("-feature", "-deprecation", "-Xlint")

libraryDependencies ++= Seq(
  "com.github.tomakehurst"   % "wiremock"      % "1.57"   % "provided",
  "io.gatling"              %% "jsonpath"      % "0.6.4",

  "org.scalatest"           %% "scalatest"     % "2.2.4"  % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" % "test"
)

// Release Settings

def teadsRepo(repo: String) = repo at s"http://nexus.teads.net/nexus/content/repositories/$repo"

publishMavenStyle     := true
pomIncludeRepository  := { _ => false }
publishTo             := Some(if(isSnapshot.value) teadsRepo("snapshots") else teadsRepo("releases"))

credentials           += Credentials(Path.userHome / ".ivy2" / ".credentials")
