name            := "wiremock-extensions"
organization    := "tv.teads"
scalaVersion    := "2.11.8"
scalacOptions   := Seq("-feature", "-deprecation", "-Xlint")

libraryDependencies ++= Seq(
  "com.github.tomakehurst"     % "wiremock-jre8-standalone" % "2.33.2" % "provided",
  "io.gatling"                %% "jsonpath"            % "0.6.9",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.9.1",
  "net.objecthunter"           % "exp4j"               % "0.4.8",
  "org.freemarker"             % "freemarker"          % "2.3.26-incubating",

  "org.scalatest"           %% "scalatest"     % "2.2.6"  % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.0" % "test"
)

fork in Test := true

// Release Settings

def teadsRepo(repo: String) = repo at s"https://nexus.teads.net/content/repositories/$repo"

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
