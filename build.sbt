scalaVersion := "3.8.2-RC1"

evictionErrorLevel := Level.Warn

libraryDependencies += "org.scala-steward" %% "scala-steward-core" % "0.37.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-no-indent",
  "-Wunused:all",
)

run / fork := true

val projectDir = file(".").getAbsolutePath

TaskKey[Unit]("selfUpdate") := {
  (Compile / runMain)
    .toTask(
      Seq(
        Seq(" org.scalasteward.core.Main"),
        Seq("--workspace", s"$projectDir/workspace"),
        Seq("--repos-file", s"$projectDir/self.md"),
        Seq("--git-author-email", "2517319+scala-steward-bot@users.noreply.github.com"),
        Seq("--forge-login", "scala-steward-bot"),
        Seq("--git-ask-pass", s"$projectDir/git_ask_pass.sh"),
        Seq("--do-not-fork"),
        Seq("--disable-sandbox"),
      ).flatten.mkString(" ")
    )
    .value
}
