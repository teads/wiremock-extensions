name            := "wiremock-extensions"
organization    := "tv.teads"
scalaVersion    := "2.11.7"
scalacOptions   := Seq("-feature", "-deprecation", "-Xlint")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.github.tomakehurst"      % "wiremock"         % "1.58" % "provided",
  "io.gatling"                  % "jsonpath"         % "0.6.5-20151211.195536-26",
  "com.fasterxml.jackson.core"  % "jackson-databind" % "2.6.3",
  "net.objecthunter"            % "exp4j"            % "0.4.5",

  "org.scalatest"           %% "scalatest"     % "2.2.5"  % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" % "test"
)

fork in Test := true

// Release Settings

def teadsRepo(repo: String) = repo at s"http://nexus.teads.net/nexus/content/repositories/$repo"

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
