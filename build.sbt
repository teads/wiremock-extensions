name            := "wiremock-extensions"
organization    := "tv.teads"
scalaVersion    := "2.11.8"
scalacOptions   := Seq("-feature", "-deprecation", "-Xlint")

libraryDependencies ++= Seq(
  "com.github.tomakehurst"     % "wiremock-standalone" % "2.5.1" % "provided",
  "io.gatling"                %% "jsonpath"            % "0.6.9",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.8.7",
  "net.objecthunter"           % "exp4j"               % "0.4.8",
  "org.freemarker"             % "freemarker"          % "2.3.25-incubating",

  "org.scalatest"           %% "scalatest"     % "2.2.6"  % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.0" % "test"
)

fork in Test := true

// Release Settings

def teadsRepo(repo: String) = repo at s"http://nexus.teads.net/content/repositories/$repo"

publishMavenStyle     := true
pomIncludeRepository  := { _ => false }
publishTo             := Some(if(isSnapshot.value) teadsRepo("snapshots") else teadsRepo("releases"))

credentials           += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Assembly Settings

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)
